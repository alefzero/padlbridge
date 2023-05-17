package com.alefzero.padlbridge.orchestrator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.config.model.PBGenericConfig;

public abstract class PBGenericService<T extends PBGenericConfig> {
	protected static final Logger logger = LogManager.getLogger();

	private T config = null;

	public final void setConfig(T config) {
		logger.trace(".setConfig [config: {}]", config);
		this.config = config;
		this.prepare();
	}

	public final T getConfig() {
		return this.config;
	}
	
	public abstract void prepare();
 
}
