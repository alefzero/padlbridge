package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.sources.PBSourceFactory;

public class LdapSourceFactory extends PBSourceFactory {

	@Override
	public String getServiceType() {
		return "ldap";
	}

	@SuppressWarnings("unchecked")
	@Override
	public LdapSourceService getInstance() {
		return new LdapSourceService();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<LdapSourceConfig> getConfigClass() {
		return LdapSourceConfig.class;
	}

}
