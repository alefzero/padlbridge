package com.alefzero.padlbridge.sources.impl;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.apache.commons.dbcp2.BasicDataSource;

import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.alefzero.padlbridge.sources.dialects.DBDialectHelper;
import com.unboundid.ldap.sdk.Entry;

public class DBSourceService extends PBSourceService {

	private DBSourceConfig config = null;
	private BasicDataSource bds = null;
	private DBDialectHelper helper = null;

	@Override
	public void prepare() {
		config = (DBSourceConfig) super.getConfig();
		if (bds == null) {
			bds = new BasicDataSource();
			bds.setUrl(config.getJdbcURL());
			bds.setUsername(config.getUsername());
			bds.setPassword(config.getPassword());
			bds.setMaxTotal(100);
			bds.setMinIdle(10);
			bds.setCacheState(false);
		}
		try (Connection conn = bds.getConnection()) {
			helper = (DBDialectHelper) Class.forName(config.getDialectHelperClass()).getDeclaredConstructor()
					.newInstance();

			helper.prepare(conn, config.getQuery(), config.getUid());

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			logger.error("Dialect class not found: {}", config.getDialectHelperClass());
			throw new PadlUnrecoverableError(e);
		} catch (SQLException e) {
			logger.error("Problem processing query SQL={}. Check your configuration.", config.getQuery());
			throw new PadlUnrecoverableError(e);
		}
	}

	@Override
	public Iterator<DataEntry> getAllEntries() {
		try (Connection conn = bds.getConnection()) {
			return new EntryIterator(conn, helper.getDBColumnsOfAttributes());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new PadlUnrecoverableError(e);
		}
	}

	private class EntryIterator implements Iterator<DataEntry> {
		private Connection conn;
		private PreparedStatement ps;
		private DataEntry currentEntry;
		private ResultSet rs;
		private Deque<String> dbColumns;

		private EntryIterator(Connection conn, Deque<String> dbColumns) throws SQLException {
			this.conn = conn;
			this.dbColumns = dbColumns;
			prepareIterator();
		}

		private void prepareIterator() throws SQLException {
			String normalizedQuery = helper.getSQLWithHash();
			ps = conn.prepareStatement(normalizedQuery);
			rs = ps.executeQuery();

		}

		private DataEntry nextEntry = null;

		@Override
		public boolean hasNext() {
			boolean hasNext = false;
			if (isLineAlreadyFetchedFromDB()) {
				currentEntry = nextEntry;
				nextEntry = null;
				hasNext = true;
			} else {
				try {
					hasNext = rs.next();
					if (hasNext) {
						currentEntry = new DataEntry(rs.getString(config.getUid()), createLdapEntryFrom(rs),
								rs.getString(DBDialectHelper.PADL_HASH_COLUMN_NAME));
					}
				} catch (SQLException e) {
					logger.error("Cannot read more data. Reason: ", e.getLocalizedMessage());
				}
			}

			if (hasNext && didConfigHasMultiValuedAttributes()) {
				try {
					while (rs.next()) {
						if (isNextLineFromSourceAnotherEntry(rs)) {
							nextEntry = new DataEntry(rs.getString(config.getUid()), createLdapEntryFrom(rs),
									rs.getString(DBDialectHelper.PADL_HASH_COLUMN_NAME));
							break;
						} else {
							for (String multiValuedAttributeName : config.getMultiValueAttributes()) {
								currentEntry.getEntry().addAttribute(multiValuedAttributeName,
										rs.getString(config.getDBAttributeNameFor(multiValuedAttributeName)));
							}
						}
					}
				} catch (SQLException e) {
					logger.error("Cannot read more data. Reason: ", e.getLocalizedMessage());
				}
			}

			return hasNext;
		}

		private boolean didConfigHasMultiValuedAttributes() {
			return config.getMultiValueAttributes().size() > 0;
		}

		private boolean isLineAlreadyFetchedFromDB() {
			return nextEntry != null;
		}

		public boolean isNextLineFromSourceAnotherEntry(ResultSet rs) throws SQLException {
			return !currentEntry.getUid().equals(rs.getString(rs.getString(config.getUid())));
		}

		private Entry createLdapEntryFrom(ResultSet rs) throws SQLException {
			Entry entry = new Entry(String.format(config.getDn(), rs.getString(config.getUid())));
			for (String dbColumn : dbColumns) {
				entry.addAttribute(config.getLdapAttributeNameFor(dbColumn), rs.getString(dbColumn));
			}
			entry.addAttribute("objectClass", config.getObjectClasses());
			return entry;
		}

		@Override
		public DataEntry next() {
			return currentEntry;
		}

	}

	@Override
	public Iterator<String> getAllUids() {
		Deque<String> uids = new ArrayDeque<String>();
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(helper.getSQLForAllUids());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				uids.add(rs.getString(1));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Throw correct exception
			e.printStackTrace();
		}
		return uids.iterator();
	}

}
