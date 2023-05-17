package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.sources.PBSourceFactory;

public class LdifSourceFactory extends PBSourceFactory {

	@Override
	public String getServiceType() {
		return "config";
	}

	@SuppressWarnings("unchecked")
	@Override
	public LdifSourceService getInstance() {
		return new LdifSourceService();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<LdifSourceConfig> getConfigClass() {
		return LdifSourceConfig.class;
	}

}
