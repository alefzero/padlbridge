package com.alefzero.padlbridge.cache.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.core.model.DataEntry;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;

public class MariaDBCacheService extends PBCacheService {

	protected static final Logger logger = LogManager.getLogger();

	private static final String SQL_CREATE_CURRENT_CACHE_TABLE = """
			create table if not exists current_cache
				(
				source_name varchar(128),
				uid varchar(128) not null,
				dn varchar(512),
				current_hash varchar(64),
				status_flag boolean default false,
				primary key (source_name, uid)
				)""";

	private static final String CREATE_CURRENT_CACHE_INDEX = """
			create index if not exists current_cache_ndx
			on current_cache (source_name, dn)""";

	private static final String SQL_UPDATE_ALL_CACHE_STATUS_TO_UNSET = """
			update current_cache
			set status_flag = ?""";

	private static final String SQL_UPDATE_CACHE_STATUS_BY_SOURCE_AND_UID = """
			update current_cache
			set status_flag = ?
			where
			    source_name = ?
				and uid = ?""";

	private static final String SQL_UPDATE_CHANGE_CACHE_STATUS_FOR_SOURCE = """
			update current_cache
			set status_flag = ?
			where
			    source_name = ?
				and status_flag = ?""";

	private static final int BATCH_COUNT = 10_000;

	private static final String SQL_GET_ALL_DNs_BY_SOURCE_AND_STATUS = """
			select dn
			from current_cache
			where
			   status_flag = ?
			   and source_name = ?""";

	private static final String SQL_DELETE_DN_FROM_SOURCE = """
			delete from current_cache
			where
				source_name = ?
				and dn = ?""";

	private static final String SQL_SELECT_GET_HASH_FROM_CACHE = """
			select current_hash
			from current_cache
			where
				source_name = ?
				and uid = ?""";

	private static final String SQL_UPDATE_HASH_VALUE_OF = """
			update current_cache
			set current_hash = ?
			where
				source_name = ?
				and uid = ?""";

	private static final String SQL_INSERT_ENTRY_TO_CACHE = """
			insert into current_cache
			( source_name, uid, dn, current_hash, status_flag )
			values ( ?, ?, ?, ?, 0 )""";

	private MariaDBCacheConfig config = null;
	private BasicDataSource bds = null;

	public MariaDBCacheService() {
		super();
	}

	@Override
	public void prepare() {
		logger.trace(".prepare");
		initializeResources();
		cleanCacheTablesState();
	}

	private void initializeResources() {
		logger.trace(".initializeResources ");

		if (bds == null || bds.isClosed()) {
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
				createDBStructure(conn);
			} catch (SQLException e) {
				// TODO Send throws as Unrecoverable
				e.printStackTrace();
			}
		}
	}

	private void createDBStructure(Connection conn) throws SQLException {
		logger.trace(".createDBStructure ");
		conn.prepareStatement(SQL_CREATE_CURRENT_CACHE_TABLE).execute();
		conn.prepareStatement(CREATE_CURRENT_CACHE_INDEX).execute();
	}

	private void cleanCacheTablesState() {
		logger.trace(".cleanCacheTablesState ");
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_ALL_CACHE_STATUS_TO_UNSET);
			ps.setInt(1, PBCacheService.CACHED_ENTRY_STATUS_UNSET);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Send throws as Unrecoverable
			logger.error("Problem processing cache: ", e);
		}
	}

	@Override
	public void syncUidsFromSource(String sourceName, Iterator<String> allDistinctUids) {
		logger.trace(".setEntryUidAsFoundFor ");
		try (Connection conn = bds.getConnection()) {
			PreparedStatement psCheckAsFound = conn.prepareStatement(SQL_UPDATE_CACHE_STATUS_BY_SOURCE_AND_UID);
			int batchCount = 0;
			while (allDistinctUids.hasNext()) {
				String uid = allDistinctUids.next();
				psCheckAsFound.setInt(1, CACHED_ENTRY_STATUS_EXISTS);
				psCheckAsFound.setString(2, sourceName);
				psCheckAsFound.setString(3, uid);
				psCheckAsFound.addBatch();

				if (++batchCount > BATCH_COUNT) {
					psCheckAsFound.executeBatch();
					batchCount = 0;
				}
			}

			if (batchCount > 0) {
				psCheckAsFound.executeBatch();
			}
			psCheckAsFound.close();

			PreparedStatement psCheckAsToDelete = conn.prepareStatement(SQL_UPDATE_CHANGE_CACHE_STATUS_FOR_SOURCE);
			psCheckAsToDelete.setInt(1, CACHED_ENTRY_STATUS_DELETE);
			psCheckAsToDelete.setString(2, sourceName);
			psCheckAsToDelete.setInt(3, CACHED_ENTRY_STATUS_UNSET);
			psCheckAsToDelete.executeUpdate();
			psCheckAsToDelete.close();

		} catch (SQLException e) {
			// TODO throw an unchecked recoverable exception or unrecoverable error
			e.printStackTrace();
		}
	}

	@Override
	public Iterator<String> getAllDNsToBeDeletedFromSource(String sourceName) {
		logger.trace(".getDeletedUidsFrom [String sourceName={}]", sourceName);
		Deque<String> uids = new ArrayDeque<String>();
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL_DNs_BY_SOURCE_AND_STATUS);
			ps.setInt(1, CACHED_ENTRY_STATUS_DELETE);
			ps.setString(2, sourceName);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				uids.add(rs.getString(1));
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO throw an unchecked recoverable exception or unrecoverable error
			e.printStackTrace();
		}
		return uids.iterator();
	}

	@Override
	public void removeFromCacheByDN(String sourceName, Deque<String> dnItems) {
		logger.trace(".removeDNFromCache ");
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(SQL_DELETE_DN_FROM_SOURCE);
			Iterator<String> iterator = dnItems.iterator();
			int count = 0;
			while (iterator.hasNext()) {
				ps.setString(1, sourceName);
				ps.setString(2, iterator.next());
				ps.addBatch();
				if (++count > BATCH_COUNT) {
					ps.executeBatch();
					count = 0;
				}
			}
			if (count > 0) {
				ps.executeBatch();
			}
			ps.close();
		} catch (SQLException e) {
			// TODO throw an unchecked recoverable exception or unrecoverable error
			e.printStackTrace();
		}
	}

	@Override
	public int getExpectedOperationFor(String sourceName, String uid, String hash) {
		int _return = 0;
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(SQL_SELECT_GET_HASH_FROM_CACHE);
			ps.setString(1, Objects.requireNonNull(sourceName));
			ps.setString(2, Objects.requireNonNull(uid));

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				_return = Objects.requireNonNull(hash).equals(rs.getString(1))
						? PBCacheService.CACHED_ENTRY_STATUS_DO_NOTHING
						: PBCacheService.CACHED_ENTRY_STATUS_UPDATE;
			} else {
				_return = PBCacheService.CACHED_ENTRY_STATUS_ADD;
			}
			rs.close();
			ps.close();

		} catch (SQLException e) {
			// TODO throw an unchecked recoverable exception or unrecoverable error
			e.printStackTrace();
		}
		return _return;
	}

	@Override
	public void updateCacheWithData(int cacheOperationValue, String sourceName, String uid, String dn, String hash) {
		logger.trace(
				".updateCacheWithData int cacheOperationValue={}, String sourceName={}, String uid={}, String dn={}, String hash={}",
				cacheOperationValue, sourceName, uid, dn, hash);
		try (Connection conn = bds.getConnection()) {
			if (cacheOperationValue == PBCacheService.CACHED_ENTRY_STATUS_UPDATE) {
				PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_HASH_VALUE_OF);
				ps.setString(1, Objects.requireNonNull(hash));
				ps.setString(2, Objects.requireNonNull(sourceName));
				ps.setString(3, Objects.requireNonNull(uid));
				ps.executeUpdate();
				ps.close();
			} else if (cacheOperationValue == PBCacheService.CACHED_ENTRY_STATUS_ADD) {
				PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ENTRY_TO_CACHE);
				ps.setString(1, Objects.requireNonNull(sourceName));
				ps.setString(2, Objects.requireNonNull(uid));
				ps.setString(3, Objects.requireNonNull(dn));
				ps.setString(4, Objects.requireNonNull(hash));
				ps.executeUpdate();
				ps.close();
			}
		} catch (SQLException e) {
			// TODO throw an unchecked recoverable exception or unrecoverable error
			e.printStackTrace();
		}
	}

	/// -------------------------------------------------------------------------------
	/// -------------------------------------------------------------------------------
	/// -------------------------------------------------------------------------------

	protected void initializeDatasource() {
		// TODO remove this example

		logger.trace(".initializeResources ");

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

				PreparedStatement psn = conn.prepareStatement("");
				PreparedStatement psc = conn.prepareStatement("");

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

					if (counter > 1000) {
						psn.executeBatch();
						psc.executeBatch();
						counter = 0;
					}
				}
				psn.executeBatch();
				psc.executeBatch();
				logger.trace(".initializeResources loaded");

				psn.close();
				psc.close();
			} catch (SQLException e) {
				logger.error("Problem processing cache: ", e);
			}
		}
	}

	class MyIterEntry implements Iterator<DataEntry> {
		// TODO remove this example

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
					current = new DataEntry(rs.getString(2),
							new Entry("dn: " + rs.getString(1), "objectClass: inetOrgPerson",
									"cn: Sir " + rs.getString(2), "sn: SN " + rs.getString(2),
									"uid: " + rs.getString(2)),
							rs.getString(3));
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

}
