package com.alefzero.padlbridge.targets.impl;

import com.alefzero.padlbridge.targets.PBTargetFactory;

public class GenericLdapTargetFactory extends PBTargetFactory {

	@Override
	public String getServiceType() {
		return "generic-ldap";
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenericLdapTargetService getInstance() {
		return new GenericLdapTargetService();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<GenericLdapTargetConfig> getConfigClass() {
		return GenericLdapTargetConfig.class;
	}

}
