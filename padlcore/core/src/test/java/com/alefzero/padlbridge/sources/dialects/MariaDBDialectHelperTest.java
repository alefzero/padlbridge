/**
 * 
 */
package com.alefzero.padlbridge.sources.dialects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author xandecelo
 *
 */
class MariaDBDialectHelperTest {
	protected static final Logger logger = LogManager.getLogger();

	private static final String DB_TEST_FILE = "test-database";

	private static BasicDataSource bds = null;
	private static MariaDBDialectHelper helper;
	private static String sqlText = "select uid, firstname, surname, customInfo from users";
	private static Deque<String> columns = new ArrayDeque<String>(
			Arrays.asList(new String[] { "uid", "firstname", "surname", "customInfo" }));
	private String uidField = "uid";

	@BeforeAll
	static void setUp() throws Exception {

		bds = new BasicDataSource();
		bds.setUrl("jdbc:sqlite:" + DB_TEST_FILE);
		bds.setMaxTotal(10);
		bds.setMinIdle(3);
		bds.setCacheState(false);

		helper = new MariaDBDialectHelper();

		try (Connection conn = bds.getConnection()) {
			conn.createStatement().execute(
					"create table if not exists  users(uid varchar(50) not null primary key, firstname varchar(50), surname varchar(50), customInfo varchar(10))");
			conn.createStatement().execute("insert or replace into users values ('user1', 'User', 'One', 'random1')");
			helper.prepare(conn, sqlText, "uid");
		}
	}

	@AfterAll
	static void finish() throws Exception {
		Path p = Paths.get(DB_TEST_FILE);
		if (Files.exists(p)) {
			Files.delete(p);
		}
	}

	/**
	 * Test method for
	 * {@link com.alefzero.padlbridge.sources.dialects.DBDialectHelper#getSQLWithHash()}.
	 */

	@Test
	void testGetSQLWithHash() {
		try {
			String _shouldMatch = String.format("select %s %s, a.* from (%s) a order by %s",
					helper.getHashFunctionFormat(columns), DBDialectHelper.PADL_HASH_COLUMN_NAME, sqlText, uidField);
			assertTrue(_shouldMatch.equalsIgnoreCase(helper.getSQLWithHash()),
					String.format("\ntested: [%s] \nshouldMatch: [%s]", helper.getSQLWithHash(), _shouldMatch));
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test method for
	 * {@link com.alefzero.padlbridge.sources.dialects.DBDialectHelper#getDBColumns()}.
	 */
	@Test
	void testGetDBColumns() {
		assertTrue(helper.getDBColumns().containsAll(columns),
				String.format("tested: %s shouldMatch: %s", helper.getDBColumns(), columns));
	}

	/**
	 * Test method for
	 * {@link com.alefzero.padlbridge.sources.dialects.DBDialectHelper#getDBColumnsOfAttributes()}.
	 */
	@Test
	void testGetDBColumnsOfAttributes() {
		Deque<String> _toTest = helper.getDBColumnsOfAttributes();
		Collection<String> _shouldMatch = columns.stream().filter(item -> !item.matches(uidField)).toList();
		assertTrue(_toTest.containsAll(_shouldMatch),
				String.format("tested: %s shouldMatch: %s", _toTest, _shouldMatch));
	}

	/**
	 * Test method for
	 * {@link com.alefzero.padlbridge.sources.dialects.DBDialectHelper#getHashFunctionFormat(java.util.Deque)}.
	 */
	@Test
	void testGetHashFunctionFormat() {
		assertTrue(String.format("md5(concat_ws('|',%s))", String.join(",", columns))
				.equalsIgnoreCase(helper.getHashFunctionFormat(columns)));
	}

	/**
	 * Test method for
	 * {@link com.alefzero.padlbridge.sources.dialects.DBDialectHelper#getOneLineQueryFormat(java.lang.String)}.
	 */
	@Test
	void testGetOneLineQueryFormat() {
		assertTrue(String.format("select a.* from (%s) a limit 1", sqlText)
				.equalsIgnoreCase(helper.getOneLineQueryFormat(sqlText)));
	}

}
