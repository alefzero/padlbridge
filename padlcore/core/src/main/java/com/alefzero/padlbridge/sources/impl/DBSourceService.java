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
			helper = (DBDialectHelper) Class.forName(config.getDialect()).getDeclaredConstructor().newInstance();

			helper.prepare(conn, config.getQuery(), config.getUid());

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			logger.error("Dialect class not found: {}", config.getDialect());
			throw new PadlUnrecoverableError(e);
		} catch (SQLException e) {
			logger.error("Problem processing query SQL={}. Check your configuration.", config.getQuery());
			throw new PadlUnrecoverableError(e);
		}
	}

	@Override
	public Iterator<DataEntry> getAllEntries() {
		try {
			return new EntryIterator(helper.getDBColumnsOfAttributes());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new PadlUnrecoverableError(e);
		} finally {
			logger.trace(".getAllEntries returning EntryIterator for {}.", config.getName());
		}
	}

	private class EntryIterator implements Iterator<DataEntry> {
		private Connection conn;
		private PreparedStatement ps;
		private ResultSet rs;
		private DataEntry currentEntry;
		private Deque<String> dbColumns;
		private Long count = 0L;

		private EntryIterator(Deque<String> dbColumns) throws SQLException {
			logger.trace("Creating iterator for {}", config.getName());
			// TODO: change to LDAP columns so datamap can map to more than 1 column
			this.conn = bds.getConnection();
			this.dbColumns = dbColumns;
			prepareIterator();
		}

		private void prepareIterator() throws SQLException {
			logger.trace(".prepareIterator - {}", config.getName());

			String normalizedQuery = helper.getSQLWithHash();
			this.ps = conn.prepareStatement(normalizedQuery);
			this.rs = ps.executeQuery();
		}

		private DataEntry nextEntry = null;

		@Override
		public boolean hasNext() {
			logger.trace(".hasNext - {}", config.getName());
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
					e.printStackTrace();
					closeIteratorConnection();
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
					e.printStackTrace();
					closeIteratorConnection();
					logger.error("Cannot read more data. Reason: ", e.getLocalizedMessage());
				}
			}
			if (!hasNext) {
				// time to say goodbye.
				closeIteratorConnection();
			}
			logger.trace(".hasNext - return {}", hasNext);
			return hasNext;
		}

		@Override
		public DataEntry next() {
			logger.trace(".next - {}", config.getName());
			if (++count % 1000L == 0) {
				logger.debug ("Processed {} items for this source.", count);
			};
			return currentEntry;
		}

		private void closeIteratorConnection() {
			logger.trace(".closeIteratorConnection");

			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("Couldn't close database source iterator connection.");
				e.printStackTrace();
			}
		}

		private boolean didConfigHasMultiValuedAttributes() {
			logger.trace(".didConfigHasMultiValuedAttributes result: {}", config.getMultiValueAttributes().size() > 0 );
			return config.getMultiValueAttributes().size() > 0;
		}

		private boolean isLineAlreadyFetchedFromDB() {
			logger.trace(".isLineAlreadyFetchedFromDB result: {}", nextEntry != null);
			return nextEntry != null;
		}

		public boolean isNextLineFromSourceAnotherEntry(ResultSet rs) throws SQLException {
			boolean _return = !currentEntry.getUid().equals(rs.getString(config.getUid()));
			logger.trace(".isLineAlreadyFetchedFromDB result: {}", _return);
			return _return ;
		}

		private Entry createLdapEntryFrom(ResultSet rs) throws SQLException {
			Entry entry = new Entry(String.format(config.getDn(), rs.getString(config.getUid())));
			for (String dbColumn : dbColumns) {
				entry.addAttribute(config.getLdapAttributeNameFor(dbColumn), rs.getString(dbColumn));
			}
			entry.addAttribute("objectClass", config.getObjectClasses());
			return entry;
		}
	}

	@Override
	public Iterator<String> getAllUids() {
		logger.trace(".getAllUids");
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
