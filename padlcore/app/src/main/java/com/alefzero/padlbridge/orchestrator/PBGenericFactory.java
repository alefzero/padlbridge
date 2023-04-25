package com.alefzero.padlbridge.orchestrator;

import com.alefzero.padlbridge.config.model.PBGenericConfig;

public interface PBGenericFactory {

	/**
	 * Must return an unique string used as TYPE for the implemented class to be
	 * created and used. This will match target type at YAML configuration file
	 * 
	 * @return
	 */
	public String getServiceType();
	
	public <T extends PBGenericService<?>> T getInstance();

	<T extends PBGenericConfig> Class<T> getConfigClass();

}
