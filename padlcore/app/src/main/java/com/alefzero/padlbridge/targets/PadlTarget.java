package com.alefzero.padlbridge.targets;

/**
 * General interface for ldap targets
 * 
 * @author xandecelo
 *
 */
public interface PadlTarget {
	
	TargetConfiguration getConfiguration();
	
	/**
	 * Must return an unique string used as ID for the implemented class to be created and used.
	 * This will match target type at YAML configuration file 
	 * @return
	 */
	String getConfigurationID();
	

}
