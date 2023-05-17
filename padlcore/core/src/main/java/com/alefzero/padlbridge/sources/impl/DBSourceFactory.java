package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.sources.PBSourceFactory;

public class DBSourceFactory extends PBSourceFactory {

	@Override
	public String getServiceType() {
		return "database";
	}

	@SuppressWarnings("unchecked")
	@Override
	public DBSourceService getInstance() {
		return new DBSourceService();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<DBSourceConfig> getConfigClass() {
		return DBSourceConfig.class;
	}

}
