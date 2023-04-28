package com.alefzero.padlbridge.sources.impl;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

		} catch (SQLException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			logger.error("Dialect class not found: {}", config.getDialectHelperClass());
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

		@Override
		public boolean hasNext() {
			boolean hasNext = false;
			try {
				hasNext = rs.next();
				if (hasNext) {
					Entry entry = new Entry(String.format(config.getDn(), rs.getString(config.getUid())));
					for (String dbColumn : dbColumns) {
						entry.addAttribute(config.getLdapAttributeNameFor(dbColumn), rs.getString(dbColumn));
					}
					entry.addAttribute("objectClass", config.getObjectClasses());
					currentEntry = new DataEntry(entry, rs.getString(helper.PADL_HASH_COLUMN_NAME));
				}
			} catch (SQLException e) {
				logger.error("Cannot read more data. Reason: ", e.getLocalizedMessage());
			}
			return hasNext;
		}

		@Override
		public DataEntry next() {
			return currentEntry;
		}

	}

}
