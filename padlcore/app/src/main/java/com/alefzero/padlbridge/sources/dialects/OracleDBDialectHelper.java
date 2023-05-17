package com.alefzero.padlbridge.sources.dialects;

import java.util.Deque;

public class OracleDBDialectHelper extends DBDialectHelper {


	@Override
	protected String getOneLineQueryFormat(String sqlText) {
		return String.format("select a.* from (%s) a fetch first 1 row only", sqlText);
	}

	@Override
	protected String getHashFunctionFormat(Deque<String> columns) {
		return String.format(" ora_hash(%s)", String.join(" || '|' || ", columns));
	}

}
