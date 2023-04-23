package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.sources.PBSourceFactory;

public class LdapSourceFactory implements PBSourceFactory {

	@Override
	public String getServiceType() {
		return "ldap";
	}

	@Override
	public ConfigSourceService getInstance() {
		return new ConfigSourceService();
	}

	@Override
	public  Class<LdapSourceConfig> getConfigClass() {
		return LdapSourceConfig.class;
	}

}
