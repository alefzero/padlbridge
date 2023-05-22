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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.alefzero.padlbridge.sources.dialects.DBDialectHelper;

public class DBSourceService extends PBSourceService {
	protected static final Logger logger = LogManager.getLogger();

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
			return new DBSourceEntryIterator(bds.getConnection(), config, helper);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new PadlUnrecoverableError(e);
		} finally {
			logger.trace(".getAllEntries returning EntryIterator for {}.", config.getName());
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
