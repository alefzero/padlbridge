package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.sources.PBSourceFactory;

public class ConfigSourceFactory extends PBSourceFactory {

	@Override
	public String getServiceType() {
		return "config";
	}

	@SuppressWarnings("unchecked")
	@Override
	public ConfigSourceService getInstance() {
		return new ConfigSourceService();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ConfigSourceConfig> getConfigClass() {
		return ConfigSourceConfig.class;
	}

}
