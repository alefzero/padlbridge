package com.alefzero.padlbridge.cache.impl;

import com.alefzero.padlbridge.cache.PBCacheFactory;
import com.alefzero.padlbridge.config.model.CacheConfig;
import com.alefzero.padlbridge.orchestrator.PBGenericService;

public class MariaDBCacheFactory implements PBCacheFactory {

	@Override
	public String getServiceType() {
		return "database-mariadb";
	}

	@SuppressWarnings("unchecked")
	@Override
	public PBGenericService<CacheConfig> getInstance() {
		return new MariaDBCacheService();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<MariaDBCacheConfig> getConfigClass() {
		return MariaDBCacheConfig.class;
	}

}
