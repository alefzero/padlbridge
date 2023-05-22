package com.alefzero.padlbridge.sources.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.sources.dialects.DBDialectHelper;
import com.unboundid.ldap.sdk.Entry;

public class DBSourceEntryIterator implements Iterator<DataEntry> {

	protected static final Logger logger = LogManager.getLogger();

	private Connection conn;
	private PreparedStatement ps;
	private ResultSet rs;
	private DataEntry currentEntry;
	private DataEntry nextEntry = null;
	private Deque<String> dbColumns;
	private Long count = 0L;
	private DBSourceConfig config;
	private DBDialectHelper helper;

	protected DBSourceEntryIterator(Connection conn, DBSourceConfig config, DBDialectHelper helper)
			throws SQLException {
		logger.trace("Creating iterator for {}", config.getName());
		// TODO: change to LDAP columns so datamap can map to more than 1 column
		this.conn = conn;
		this.helper = helper;
		this.dbColumns = helper.getDBColumnsOfAttributes();
		this.config = config;
		prepareIterator();
	}

	private void prepareIterator() throws SQLException {
		logger.trace(".prepareIterator - {}", config.getName());
		String normalizedQuery = helper.getSQLWithHash();
		this.ps = conn.prepareStatement(normalizedQuery);
		this.rs = ps.executeQuery();
	}

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
			logger.debug("Processed {} items for this source.", count);
		}
		;
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
		logger.trace(".didConfigHasMultiValuedAttributes result: {}", config.getMultiValueAttributes().size() > 0);
		return config.getMultiValueAttributes().size() > 0;
	}

	private boolean isLineAlreadyFetchedFromDB() {
		logger.trace(".isLineAlreadyFetchedFromDB result: {}", nextEntry != null);
		return nextEntry != null;
	}

	public boolean isNextLineFromSourceAnotherEntry(ResultSet rs) throws SQLException {
		boolean _return = !currentEntry.getUid().equals(rs.getString(config.getUid()));
		logger.trace(".isNextLineFromSourceAnotherEntry result: {}", _return);
		return _return;
	}

	private Entry createLdapEntryFrom(ResultSet rs) throws SQLException {
		logger.trace(".createLdapEntryFrom");
		Entry entry = new Entry(String.format(config.getDn(), rs.getString(config.getUid())));
		logger.trace(".createLdapEntryFrom:entry");
		for (String dbColumn : dbColumns) {
			logger.trace(".createLdapEntryFrom:addAttribute {}, {}", config.getLdapAttributeNameFor(dbColumn),
					rs.getString(dbColumn));
			entry.addAttribute(config.getLdapAttributeNameFor(dbColumn), rs.getString(dbColumn));
		}
		logger.trace(".createLdapEntryFrom:objectClass {}", config.getObjectClasses());
		entry.addAttribute("objectClass", config.getObjectClasses());
		logger.trace(".createLdapEntryFrom result: {}", entry);
		return entry;
	}
}