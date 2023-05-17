package com.alefzero.padlbridge.sources.dialects;

import java.util.Deque;

public class MariaDBDialectHelper extends DBDialectHelper {


	@Override
	protected String getOneLineQueryFormat(String sqlText) {
		return String.format("select a.* from (%s) a limit 1", sqlText);
	}

	@Override
	protected String getHashFunctionFormat(Deque<String> columns) {
		return String.format("md5(concat_ws('|',%s))", String.join(",", columns));
	}

}
