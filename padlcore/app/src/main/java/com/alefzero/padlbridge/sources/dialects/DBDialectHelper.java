package com.alefzero.padlbridge.sources.dialects;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class DBDialectHelper {

	public static final String PADL_HASH_COLUMN_NAME = "padl_hash_id";
	private static final String PADL_HASH_SQL_FORMAT = "select %s %s, a.* from (%s) a order by %s";

	private String sqlText;
	private String uidField;
	private Deque<String> columns;
	private Deque<String> attributeColumns;

	public void prepare(Connection conn, String sqlText, String uidField) throws SQLException {
		this.sqlText = sqlText;
		this.uidField = uidField;

		PreparedStatement ps = conn.prepareStatement(getOneLineQueryFormat(sqlText));
		ResultSet rs = ps.executeQuery();
		ResultSetMetaData metadata = rs.getMetaData();

		columns = new ArrayDeque<String>();
		for (int i = 1; i <= metadata.getColumnCount(); i++) {
			columns.add(metadata.getColumnName(i));
		}
		rs.close();
		ps.close();
		attributeColumns = new ArrayDeque<String>(columns);
		attributeColumns.remove(uidField);
	}

	public String getSQLWithHash() throws SQLException {
		String _return = String.format(PADL_HASH_SQL_FORMAT, generateHashColumn(), PADL_HASH_COLUMN_NAME, sqlText,
				uidField);
		return _return;
	}

	private String generateHashColumn() throws SQLException {
		return getHashFunctionFormat(columns);
	}

	public Deque<String> getDBColumns() {
		return columns;
	}

	public Deque<String> getDBColumnsOfAttributes() {
		return attributeColumns;
	}

	protected abstract String getHashFunctionFormat(Deque<String> columns);

	protected abstract String getOneLineQueryFormat(String sqlText);

}