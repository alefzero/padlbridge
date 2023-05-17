package com.alefzero.padlbridge.targets.impl;

import com.alefzero.padlbridge.targets.PBTargetFactory;

public class DoNothingTargetFactory extends PBTargetFactory {

	@Override
	public String getServiceType() {
		return "do-nothing";
	}

	@SuppressWarnings("unchecked")
	@Override
	public DoNothingTargetService getInstance() {
		return new DoNothingTargetService();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<DoNothingTargetConfig> getConfigClass() {
		return DoNothingTargetConfig.class;
	}

}
