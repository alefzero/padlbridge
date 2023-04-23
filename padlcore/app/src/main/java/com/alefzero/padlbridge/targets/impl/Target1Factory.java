package com.alefzero.padlbridge.targets.impl;

import com.alefzero.padlbridge.orchestrator.PBGenericService;
import com.alefzero.padlbridge.targets.PBTargetFactory;

public class Target1Factory implements PBTargetFactory {

	@Override
	public String getServiceType() {
		return "target1";
	}

	@Override
	public PBGenericService getInstance() {
		return new Target1Service();
	}

	@Override
	public Class<Target1Config> getConfigClass() {
		return Target1Config.class;
	}

}
