package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.sources.PBSourceFactory;

public class ConfigSourceFactory implements PBSourceFactory {

	@Override
	public String getServiceType() {
		return "config";
	}

	@Override
	public ConfigSourceService getInstance() {
		return new ConfigSourceService();
	}

	@Override
	public  Class<ConfigSourceConfig> getConfigClass() {
		return ConfigSourceConfig.class;
	}

}
