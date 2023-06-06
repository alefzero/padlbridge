package com.alefzero.padlbridge.targets.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.targets.LDAPCodes;
import com.alefzero.padlbridge.targets.PBTargetService;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class GenericLdapTargetService extends PBTargetService {
	protected static final Logger logger = LogManager.getLogger();

	private LDAPConnectionPool pool = null;
	private GenericLdapTargetConfig config = null;

	private LDAPConnection getConnection() throws LDAPException {
		if (pool == null) {
			prepare();
			return pool.getConnection();
		} else {
			throw new LDAPException(ResultCode.CONNECT_ERROR);
		}

	}

	@Override
	public void prepare() {
		config = (GenericLdapTargetConfig) super.getConfig();
		try {
			LDAPConnection conn = new LDAPConnection(config.getHost(), config.getPort(), config.getAdminUser(),
					config.getAdminPassword());
			pool = new LDAPConnectionPool(conn, 10);
		} catch (LDAPException e) {
			e.printStackTrace();
			logger.error("Error creating target LDAP connections.", e);
		}
	}

	@Override
	public void add(Entry entry) {
		try (LDAPConnection conn = this.getConnection()) {
			conn.add(entry);
		} catch (LDAPException e) {
			if (e.getResultCode().intValue() == LDAPCodes.ENTRY_ALREADY_EXISTS) {
				logger.error("dn {} already exists at LDAP. Add will be ignored.", entry);
			} else {
				logger.error("Error processing target LDAP operation - dn {}. {}  {}", entry, e.getResultString(),
						e.getResultCode());
			}
		}
	}

	@Override
	public void modify(Entry entry) {
		try (LDAPConnection conn = this.getConnection()) {
			List<Modification> mods = new ArrayList<Modification>();
			for (Attribute attribute : entry.getAttributes()) {
				mods.add(new Modification(ModificationType.REPLACE, attribute.getName(), attribute.getValues()));
			}
			ModifyRequest request = new ModifyRequest(entry.getDN(), mods);
			conn.modify(request);
		} catch (LDAPException e) {
			if (e.getResultCode().intValue() == LDAPCodes.NO_SUCH_OBJECT) {
				logger.error("modification to dn {} couldnt be found LDAP - attributes: {} . Add will be ignored.",
						entry, entry.getAttributes());
			} else {
				logger.error("Error processing target LDAP operation - dn {}. {}  {}", entry, e.getResultString(),
						e.getResultCode());
			}
		}

	}

	@Override
	public void delete(Entry entry) {
		deleteTree(entry.getDN());
	}

	@Override
	public void addAll(Iterator<Entry> entriesToAddFrom) {
		Entry item = null;
		try (LDAPConnection conn = this.getConnection()) {
			while (entriesToAddFrom.hasNext()) {
				item = entriesToAddFrom.next();
				try {
					conn.add(item);
				} catch (LDAPException e) {
					if (e.getResultCode().intValue() == LDAPCodes.ENTRY_ALREADY_EXISTS) {
						logger.error("dn {} already exists at LDAP. Add will be ignored.", item);
					} else {
						throw e;
					}
				}
			}
		} catch (LDAPException e) {
			logger.error("Error processing target LDAP operation - dn {}. {}  {}", item, e.getResultString(),
					e.getResultCode());
		}
	}

	@Override
	public Deque<String> deleteAll(Iterator<String> listOfDNsToDelete) {
		Deque<String> _return = new ArrayDeque<String>();
		String item = null;
		try (LDAPConnection conn = this.getConnection()) {
			while (listOfDNsToDelete.hasNext()) {
				item = listOfDNsToDelete.next();
				try {
					logger.trace("Trying to delete item dn={}", item);
					conn.delete(item);
					_return.add(item);

				} catch (LDAPException e) {
					if (e.getResultCode().intValue() == LDAPCodes.NOT_ALLOWED_ON_NON_LEAF) {
						e.printStackTrace();
						logger.trace("deleting recursively for {}", item);
						deleteTree(item);
						_return.add(item);
					} else if (e.getResultCode().intValue() == LDAPCodes.NO_SUCH_OBJECT) {
						logger.error("dn {} does not exist at LDAP. Delete will be ignored.", item);
					} else {
						throw e;
					}
				}
			}
		} catch (LDAPException e) {
			e.printStackTrace();
			logger.error("Error processing target LDAP operation - dn {}. {}  {}", item, e.getResultString(),
					e.getResultCode());
		}
		return _return;
	}

	public void deleteTree(String dn) {
		try (LDAPConnection conn = this.getConnection()) {
			SearchResult result = conn.search(dn, SearchScope.ONE, "(objectClass=*)", "dn");
			for (SearchResultEntry item : result.getSearchEntries()) {
				String todel = item.getDN();
				try {
					conn.delete(todel);
				} catch (LDAPException e) {
					if (e.getResultCode().intValue() == LDAPCodes.NOT_ALLOWED_ON_NON_LEAF) {
						e.printStackTrace();
						logger.trace("deleting recursively for {}", todel);
						deleteTree(todel);
					} else if (e.getResultCode().intValue() == LDAPCodes.NO_SUCH_OBJECT) {
						// do nothing
					}
				}
			}
			conn.delete(dn);

		} catch (LDAPException e) {
			e.printStackTrace();
		}
	}

}
