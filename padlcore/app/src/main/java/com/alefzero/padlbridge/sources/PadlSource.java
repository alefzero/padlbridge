package com.alefzero.padlbridge.sources;

/**
 * General interface for data sources
 * @author xandecelo
 */
public interface PadlSource {
	
	SourceConfiguration getConfiguration();
	
	/**
	 * Must return an unique string used as ID for the implemented class to be created and used.
	 * This will match source type at YAML configuration file 
	 * @return
	 */
	String getConfigurationID();
	
}
