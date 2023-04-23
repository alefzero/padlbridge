package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.sources.PBSourceFactory;

public class Source1Factory implements PBSourceFactory {

	@Override
	public String getServiceId() {
		return "source1";
	}

	@Override
	public Source1Service getInstance() {
		return new Source1Service();
	}

}
