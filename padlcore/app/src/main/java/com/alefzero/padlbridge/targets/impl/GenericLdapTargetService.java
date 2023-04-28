package com.alefzero.padlbridge.targets.impl;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.targets.LDAPCodes;
import com.alefzero.padlbridge.targets.PBTargetService;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldif.LDIFException;

public class GenericLdapTargetService extends PBTargetService {
	protected static final Logger logger = LogManager.getLogger();

	private LDAPConnectionPool pool = null;
	private GenericLdapTargetConfig config = null;
	private boolean initialized = false;

	private void init() {
		config = (GenericLdapTargetConfig) super.getConfig();
		try {
			LDAPConnection conn = new LDAPConnection(config.getHost(), config.getPort(), config.getAdminUser(),
					config.getAdminPassword());
			pool = new LDAPConnectionPool(conn, 10);
			addTestData();
		} catch (LDAPException e) {
			logger.error("Error creating target LDAP connections.", e);
		}

	}

	private void addTestData() {
		try (LDAPConnection conn = pool.getConnection()) {
			deleteTree("dc=alefzero,dc=com");
			conn.add(new Entry("dn: dc=alefzero,dc=com", "objectClass: domain", "objectClass: top", "dc: alefzero"));
			conn.add(new Entry("dn: ou=users,dc=alefzero,dc=com", "objectClass: organizationalUnit", "objectClass: top",
					"ou: users"));
		} catch (LDAPException e) {
			if (e.getResultCode().intValue() == LDAPCodes.ENTRY_ALREADY_EXISTS) {
				logger.error("config dn already exists at LDAP. Add will be ignored.");
			} else {
				e.printStackTrace();
			}
		} catch (LDIFException e) {
			e.printStackTrace();
		}
	}

	public void deleteTree(String dn) {
		try (LDAPConnection conn = pool.getConnection()) {
			SearchResult result = conn.search(dn, SearchScope.ONE, "(objectClass=*)", "dn");
			for (SearchResultEntry item : result.getSearchEntries()) {
				String todel = item.getDN();
				try {
					conn.delete(todel);
				} catch (LDAPException e) {
					if (e.getResultCode().intValue() == LDAPCodes.NOT_ALLOWED_ON_NON_LEAF) {
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

	@Override
	public void deleteAll(Iterator<String> listOfDeletedDN) {
		if (!initialized)
			init();
		String item = null;
		try (LDAPConnection conn = pool.getConnection()) {
			while (listOfDeletedDN.hasNext()) {
				item = listOfDeletedDN.next();
				try {
					logger.trace("Trying to delete item dn={}", item);
					conn.delete(item);
				} catch (LDAPException e) {
					if (e.getResultCode().intValue() == LDAPCodes.NO_SUCH_OBJECT) {
						logger.error("dn {} does not exist at LDAP. Delete will be ignored.", item);
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
	public void addAll(Iterator<Entry> entriesToAddFrom) {
		if (!initialized)
			init();
		Entry item = null;
		try (LDAPConnection conn = pool.getConnection()) {
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
	public void modifyAll(Iterator<Entry> entriesToModifyFrom) {

	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

}
