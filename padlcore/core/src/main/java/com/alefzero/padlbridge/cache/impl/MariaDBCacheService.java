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
import com.alefzero.padlbridge.config.model.OperationalActions;
import com.alefzero.padlbridge.util.PInfo;

public class MariaDBCacheService extends PBCacheService {

	protected static final Logger logger = LogManager.getLogger();

	private static final String SQL_CREATE_CURRENT_CACHE_TABLE = """
			create table if not exists $INSTANCE_NAME$
				(
				source_name varchar(128),
				uid varchar(128) not null,
				dn varchar(512),
				current_hash varchar(64),
				status_flag boolean default false,
				primary key (source_name, uid)
				)""";

	private static final String CREATE_CURRENT_CACHE_INDEX = """
			create index if not exists $INSTANCE_NAME$_ndx
			on $INSTANCE_NAME$ (source_name, dn)""";

	private static final String SQL_UPDATE_ALL_CACHE_STATUS_TO_UNSET = """
			update $INSTANCE_NAME$
			set status_flag = ?""";

	private static final String SQL_UPDATE_CACHE_STATUS_BY_SOURCE_AND_UID = """
			update $INSTANCE_NAME$
			set status_flag = ?
			where
			    source_name = ?
				and uid = ?""";

	private static final String SQL_UPDATE_CHANGE_CACHE_STATUS_FOR_SOURCE = """
			update $INSTANCE_NAME$
			set status_flag = ?
			where
			    source_name = ?
				and status_flag = ?""";

	private static final int BATCH_COUNT = 10_000;

	private static final String SQL_GET_ALL_DNs_BY_SOURCE_AND_STATUS = """
			select dn
			from $INSTANCE_NAME$
			where
			   status_flag = ?
			   and source_name = ?""";

	private static final String SQL_DELETE_DN_FROM_SOURCE = """
			delete from $INSTANCE_NAME$
			where
				source_name = ?
				and dn = ?""";

	private static final String SQL_SELECT_GET_HASH_FROM_CACHE = """
			select current_hash
			from $INSTANCE_NAME$
			where
				source_name = ?
				and uid = ?""";

	private static final String SQL_UPDATE_HASH_VALUE_OF = """
			update $INSTANCE_NAME$
			set current_hash = ?
			where
				source_name = ?
				and uid = ?""";

	private static final String SQL_INSERT_ENTRY_TO_CACHE = """
			insert into $INSTANCE_NAME$
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

	public String formatSQL(String sqlCreateCurrentCacheTable) {
		logger.trace(".formatSQL [{}}] + instanceName: [{}]", sqlCreateCurrentCacheTable, getInstanceName());
		String _return = sqlCreateCurrentCacheTable.replace("$INSTANCE_NAME$", "cache_data_" + getInstanceName());
		logger.trace(".formatSQL [return: {}]", _return);
		return _return;
	}

	private void createDBStructure(Connection conn) throws SQLException {
		logger.trace(".createDBStructure ");
		conn.prepareStatement(formatSQL(SQL_CREATE_CURRENT_CACHE_TABLE)).execute();
		conn.prepareStatement(formatSQL(CREATE_CURRENT_CACHE_INDEX)).execute();
	}

	private void cleanCacheTablesState() {
		logger.trace(".cleanCacheTablesState ");
		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(formatSQL(SQL_UPDATE_ALL_CACHE_STATUS_TO_UNSET));
			ps.setInt(1, OperationalActions.UNSET.getOperationalValue());
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
			PreparedStatement psCheckAsFound = conn
					.prepareStatement(formatSQL(SQL_UPDATE_CACHE_STATUS_BY_SOURCE_AND_UID));
			int batchCount = 0;
			while (allDistinctUids.hasNext()) {
				String uid = allDistinctUids.next();
				psCheckAsFound.setInt(1, OperationalActions.EXISTS.getOperationalValue());
				psCheckAsFound.setString(2, sourceName);
				psCheckAsFound.setString(3, uid);
				psCheckAsFound.addBatch();

				if (++batchCount > BATCH_COUNT) {
					logger.trace(PInfo.log("cache.batch-commit-message"));
					psCheckAsFound.executeBatch();
					batchCount = 0;
				}
			}

			if (batchCount > 0) {
				psCheckAsFound.executeBatch();
			}
			psCheckAsFound.close();

			PreparedStatement psCheckAsToDelete = conn
					.prepareStatement(formatSQL(SQL_UPDATE_CHANGE_CACHE_STATUS_FOR_SOURCE));
			psCheckAsToDelete.setInt(1, OperationalActions.DELETE.getOperationalValue());
			psCheckAsToDelete.setString(2, sourceName);
			psCheckAsToDelete.setInt(3, OperationalActions.UNSET.getOperationalValue());
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
			PreparedStatement ps = conn.prepareStatement(formatSQL(SQL_GET_ALL_DNs_BY_SOURCE_AND_STATUS));
			ps.setInt(1, OperationalActions.DELETE.getOperationalValue());
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
			PreparedStatement ps = conn.prepareStatement(formatSQL(SQL_DELETE_DN_FROM_SOURCE));
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
	public OperationalActions getExpectedOperationFor(OperationalActions defaultAddOperation, String sourceName,
			String uid, String hash) {
		OperationalActions _return = defaultAddOperation;

		try (Connection conn = bds.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(formatSQL(SQL_SELECT_GET_HASH_FROM_CACHE));
			ps.setString(1, Objects.requireNonNull(sourceName));
			ps.setString(2, Objects.requireNonNull(uid));

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				_return = Objects.requireNonNull(hash).equals(rs.getString(1)) ? OperationalActions.DO_NOTHING
						: OperationalActions.UPDATE;
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
	public void updateCacheWithData(OperationalActions operationalAction, String sourceName, String uid, String dn,
			String hash) {
		logger.trace(
				".updateCacheWithData int cacheOperationValue={}, String sourceName={}, String uid={}, String dn={}, String hash={}",
				operationalAction, sourceName, uid, dn, hash);
		try (Connection conn = bds.getConnection()) {
			if (operationalAction == OperationalActions.UPDATE) {
				PreparedStatement ps = conn.prepareStatement(formatSQL(SQL_UPDATE_HASH_VALUE_OF));
				ps.setString(1, Objects.requireNonNull(hash));
				ps.setString(2, Objects.requireNonNull(sourceName));
				ps.setString(3, Objects.requireNonNull(uid));
				ps.executeUpdate();
				ps.close();
			} else if (operationalAction == OperationalActions.ADD || operationalAction == OperationalActions.REPLACE) {
				PreparedStatement ps = conn.prepareStatement(formatSQL(SQL_INSERT_ENTRY_TO_CACHE));
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
}
