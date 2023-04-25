package com.alefzero.padlbridge.orchestrator;

import com.alefzero.padlbridge.config.model.PBGenericConfig;

public abstract class PBGenericService<T extends PBGenericConfig> {
	
	private T config = null;
	
	public void setConfig(T config) {
		this.config = config;
	}

	public T getConfig() {
		return this.config;
	}
}
