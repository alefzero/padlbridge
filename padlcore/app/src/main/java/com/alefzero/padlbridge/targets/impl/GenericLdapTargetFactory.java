package com.alefzero.padlbridge.targets.impl;

import com.alefzero.padlbridge.orchestrator.PBGenericService;
import com.alefzero.padlbridge.targets.PBTargetFactory;

public class GenericLdapTargetFactory implements PBTargetFactory {

	@Override
	public String getServiceType() {
		return "generic-ldap";
	}

	@Override
	public PBGenericService getInstance() {
		return new GenericLdapTargetService();
	}

	@Override
	public Class<GenericLdapTargetConfig> getConfigClass() {
		return GenericLdapTargetConfig.class;
	}

}
