package com.alefzero.padlbridge.sources.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;

import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class LdapSourceService extends PBSourceService {

	private LDAPConnectionPool pool = null;
	private LdapSourceConfig config;

	@Override
	public void prepare() {
		config = (LdapSourceConfig) super.getConfig();
		try {
			LDAPConnection conn = new LDAPConnection(config.getHost(), config.getPort(), config.getUsername(),
					config.getPassword());
			pool = new LDAPConnectionPool(conn, 10);
		} catch (LDAPException e) {
			logger.error("Error creating target LDAP connections.", e);
		}
	}

	@Override
	public Iterator<DataEntry> getAllEntries() {
		try (LDAPConnection conn = pool.getConnection()) {
			SearchResult result = conn.search(config.getBaseDN(), SearchScope.SUB, "(objectClass=*)",
					"*");
			return new EntryIterator(result);
		} catch (LDAPException e) {
			e.printStackTrace();
			throw new PadlUnrecoverableError(e);
		}
	}

	@Override
	public Iterator<String> getAllUids() {
		Deque<String> dnItems = new ArrayDeque<String>();
		try (LDAPConnection conn = pool.getConnection()) {
			SearchResult result = conn.search(config.getBaseDN(), SearchScope.SUB, "(objectClass=*)",
					"dn");
			result.getSearchEntries().forEach(entry -> {
				dnItems.add(entry.getDN());
			});

		} catch (LDAPException e) {
			// TODO: throw exception and treat.
			e.printStackTrace();
		}
		return dnItems.iterator();
	}

	class EntryIterator implements Iterator<DataEntry> {

		private Iterator<SearchResultEntry> iterator;

		private EntryIterator(SearchResult result) {
			this.iterator = result.getSearchEntries().iterator();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public DataEntry next() {
			SearchResultEntry entry = iterator.next();
			DataEntry dataEntry = new DataEntry(entry.getDN(), entry, DigestUtils.md5Hex(entry.toLDIFString()));
			return dataEntry;
		}

	}
}
