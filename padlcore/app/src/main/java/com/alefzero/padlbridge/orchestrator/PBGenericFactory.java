package com.alefzero.padlbridge.orchestrator;

public interface PBGenericFactory {
	
	/**
	 * Must return an unique string used as TYPE for the implemented class to be
	 * created and used. This will match target type at YAML configuration file
	 * 
	 * @return
	 */
	public String getServiceType();

	public PBGenericService getInstance();
	
}
