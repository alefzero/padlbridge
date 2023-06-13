package com.alefzero.padlbridge.cache.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.cache.PBCacheFactory;
import com.alefzero.padlbridge.config.model.CacheConfig;
import com.alefzero.padlbridge.orchestrator.PBGenericService;

/**
 * Factory for Maria DB cache services instances
 * 
 * @author xandecelo
 *
 */
public class MariaDBCacheFactory implements PBCacheFactory {
	protected static final Logger logger = LogManager.getLogger();

	@Override
	public String getServiceType() {
		return "database-mariadb";
	}

	@SuppressWarnings("unchecked")
	@Override
	public PBGenericService<CacheConfig> getInstance() {
		logger.trace(".getInstance");
		return new MariaDBCacheService();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<MariaDBCacheConfig> getConfigClass() {
		logger.trace(".getConfigClass");
		return MariaDBCacheConfig.class;
	}

}
