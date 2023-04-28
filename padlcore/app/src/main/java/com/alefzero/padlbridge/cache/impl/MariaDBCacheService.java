package com.alefzero.padlbridge.cache.impl;

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

import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;

public class MariaDBCacheService extends PBCacheService {

	protected static final Logger logger = LogManager.getLogger();

	private MariaDBCacheConfig config = null;

	private static final String SQL_CREATE_NEW_CACHE_TABLE = "create table if not exists new_cache (config_id varchar(128), dn varchar(512), uid varchar(128) not null, hash varchar(64), removed_line_flag boolean default false, primary key (uid, hash))";
	private static final String SQL_CREATE_CURRENT_CACHE_TABLE = "create table if not exists current_cache (config_id varchar(128), dn varchar(512), uid varchar(128) not null, hash varchar(64), removed_line_flag boolean default false, primary key (uid, hash))";
	private static final String SQL_DROP_NEW_CACHE_TABLE = "drop table if exists new_cache";
	private static final String SQL_INSERT_INTO_NEW_CACHE = "insert ignore into new_cache (config_id, dn, uid, hash, removed_line_flag) values (?,?,?,?,?)";
	private static final String SQL_INSERT_INTO_CURRENT_CACHE = "insert ignore into current_cache (config_id, dn, uid, hash, removed_line_flag) values (?,?,?,?,?)";
	private static final String SQL_FLAG_DELETED_LINES_FROM_CURRENT_CACHE = "update current_cache c "
			+ " set removed_line_flag = true " + " where not exists " + " (select n.uid from new_cache n "
			+ " where n.config_id = c.config_id  and n.hash = c.hash )";

//	private static final String SQL_GET_DELETED_DN_FROM_CACHE_CHANGES = "select distinct c.dn "
//			+ " from current_cache c " + " where not exists " + " (select n.dn from new_cache n "
//			+ " where n.config_id = c.config_id and n.dn = c.dn )";

	private static final String SQL_GET_DELETED_DN_FROM_CACHE_CHANGES = """
			select distinct c.dn from current_cache c  left join new_cache n
			on n.config_id = c.config_id and n.dn = c.dn
			where n.dn is null
			""";
	private static final String SQL_GET_CHANGED_UID_FROM_CACHE_CHANGES = "select distinct n.uid, n.dn "
			+ " from new_cache n " + " where not exists " + " (select c.uid from current_cache c "
			+ " where c.config_id = n.config_id " + "   and c.hash      = n.hash " + "   and c.uid       = n.uid )";

	private static final String SQL_CHANGE_REMOVE_FLAG_FROM_UID = "update current_cache set removed_line_flag = false where uid = ? and removed_line_flag = true";

	private static final String SQL_DELETE_FLAGGED_CURRENT_CACHE = "delete from current_cache where removed_line_flag = true";

	private static final String CREATE_NEW_CACHE_INDEX = "create index if not exists new_cache_ndx on new_cache (config_id, dn)";

	private static final String CREATE_CURRENT_CACHE_INDEX = "create index if not exists current_cache_ndx on current_cache (config_id, dn)";

	private static final int CACHE_BATCH_LIMIT = 1000;

	private BasicDataSource bds = null;

	public MariaDBCacheService() {
		super();
	}

	private void initializeDatasource() {
		logger.trace(".initializeDatasource ");

		if (bds == null) {
			logger.debug("Loading cache datasource.");
			config = (MariaDBCacheConfig) super.getConfig();
			bds = new BasicDataSource();
			bds.setUrl(config.getJdbcURL());
			bds.setUsername(config.getUsername());
			bds.setPassword(config.getPassword());
			bds.setMaxTotal(100);
			bds.setMinIdle(10);
			bds.setCacheState(false);

			try (Connection conn = bds.getConnection()) {
				conn.prepareStatement(SQL_CREATE_CURRENT_CACHE_TABLE).execute();
				conn.prepareStatement(CREATE_CURRENT_CACHE_INDEX).execute();
				conn.prepareStatement(SQL_CREATE_NEW_CACHE_TABLE).execute();
				conn.prepareStatement(CREATE_NEW_CACHE_INDEX).execute();

				PreparedStatement psn = conn.prepareStatement(SQL_INSERT_INTO_NEW_CACHE);
				PreparedStatement psc = conn.prepareStatement(SQL_INSERT_INTO_CURRENT_CACHE);
				int counter = 0;
				logger.trace(".initializeDatasource loading");
				for (int i = 0; i < 10_000; i++) {
					counter++;

					// config_id, dn, uid, hash, removed_line_flag
					int k = 1;
					psc.setString(k++, "config1");
					psc.setString(k++, "uid=user_" + i + ",ou=users,dc=alefzero,dc=com");
					psc.setString(k++, "user_" + i);
					psc.setString(k++, "hash_user_" + i);
					psc.setBoolean(k++, false);
					psc.addBatch();

					if (counter % 9999 != 0) {
						k = 1;
						psn.setString(k++, "config1");
						psn.setString(k++, "uid=user_" + i + ",ou=users,dc=alefzero,dc=com");
						psn.setString(k++, "user_" + i);
						psn.setString(k++, "hash_user_" + i);
						psn.setBoolean(k++, false);
						psn.addBatch();
					}

					if (counter > CACHE_BATCH_LIMIT) {
						psn.executeBatch();
						psc.executeBatch();
						counter = 0;
					}
				}
				psn.executeBatch();
				psc.executeBatch();
				logger.trace(".initializeDatasource loaded");

				psn.close();
				psc.close();
			} catch (SQLException e) {
				logger.error("Problem processing cache: ", e);
			}
		}
	}

	@Override
	public void prepare() {
		logger.trace(".prepare");
		initializeDatasource();

		try (Connection conn = bds.getConnection()) {
//			conn.prepareStatement(SQL_DROP_NEW_CACHE_TABLE).executeUpdate();
//			conn.prepareStatement(SQL_CREATE_NEW_CACHE_TABLE).executeUpdate();
//			conn.prepareStatement(CREATE_NEW_CACHE_INDEX).executeUpdate();
		} catch (SQLException e) {
			logger.error("Problem processing cache: ", e);
		}
	}

	@Override
	public void addHashesFrom(PBSourceService source) {
		logger.trace(".addHashesFrom");

//		try (Connection conn = bds.getConnection()) {
//			PreparedStatement ps = conn.prepareStatement(SQL_INSERT_INTO_CURRENT_CACHE);
//
//			int counter = 0;
//			while (source.getAllHashes().hasNext()) {
//				counter++;
//				CacheEntry entry = source.getAllHashes().next();
//				ps.setString(1, source.getConfig().getName());
//				ps.setString(2, entry.getDn());
//				ps.setString(3, entry.getUid());
//				ps.setString(4, entry.getHash());
//				ps.setBoolean(5, false);
//				ps.addBatch();
//				if (counter > CACHE_BATCH_LIMIT) {
//					ps.executeBatch();
//					counter = 0;
//				}
//			}
//			ps.executeBatch();
//			ps.close();
//
//		} catch (SQLException e) {
//			logger.error("Problem processing cache: ", e);
//		}

	}

	class MyIterEntry implements Iterator<DataEntry> {

		private Connection conn;
		private ResultSet rs;
		private PreparedStatement ps;
		private DataEntry current = null;

		MyIterEntry(Connection conn) throws SQLException {
			this.conn = conn;
			init();
		}

		private void init() throws SQLException {
			ps = conn.prepareStatement(
					"select distinct dn, uid, hash from current_cache where config_id = ? and uid = ? order by 1,2");
			ps.setString(1, "config1");
			rs = ps.executeQuery();
		}

		@Override
		public boolean hasNext() {
			boolean _return = false;
			try {
				_return = rs.next();
				if (_return) {
					current = new DataEntry(new Entry("dn: "+ rs.getString(1)
							, "objectClass: inetOrgPerson"
							, "cn: Sir " + rs.getString(2)
							,"sn: SN " + rs.getString(2)
							, "uid: " + rs.getString(2))
							,  rs.getString(3));
				}
			} catch (SQLException | LDIFException e) {
				e.printStackTrace();
			}
			return _return;
		}

		@Override
		public DataEntry next() {
			return current;
		}

	}

	class MyIterString implements Iterator<String> {

		private Connection conn;
		private ResultSet rs;
		private PreparedStatement ps;
		private String current = null;

		MyIterString(Connection conn) throws SQLException {
			this.conn = conn;
			init();
		}

		private void init() throws SQLException {
			ps = conn.prepareStatement(SQL_GET_DELETED_DN_FROM_CACHE_CHANGES);
			rs = ps.executeQuery();
		}

		@Override
		public boolean hasNext() {
			boolean _return = false;
			try {
				_return = rs.next();
				if (_return) {
					current = rs.getString(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return _return;
		}

		@Override
		public String next() {
			return current;
		}

	}

	@Override
	public Iterator<String> getDeletedEntriesFrom(PBSourceService source) {
		logger.trace(".getDeletedEntriesFrom");
		Deque<String> result = new ArrayDeque<String>();
		logger.trace("bds {} {} ", bds.getNumIdle(), bds.getNumActive());

		try (Connection conn = bds.getConnection()) {
			logger.trace("bds {} {} ", bds.getNumIdle(), bds.getNumActive());
			return new MyIterString(conn);
		} catch (SQLException e) {
			logger.error("Problem processing cache: ", e);
		} finally {
			logger.trace("bds {} {} ", bds.getNumIdle(), bds.getNumActive());

		}
		return result.iterator();
	}

	@Override
	public void consolidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<DataEntry> getEntriesToAddOrModify(PBSourceService source) {
		logger.trace(".getEntriesToAddFrom");
		// TODO check hashes to set operation
		Deque<DataEntry> result = new ArrayDeque<DataEntry>();
		try (Connection conn = bds.getConnection()) {
			return new MyIterEntry(conn);
		} catch (SQLException e) {
			logger.trace("bds {} {} ", bds.getNumIdle(), bds.getNumActive());
		}
		return result.iterator();
	}

	@Override
	public void updateTables() {
		// TODO Auto-generated method stub

	}

}
