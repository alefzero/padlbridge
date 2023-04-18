package com.alefzero.padlbridge.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum EnvironmentOptions {

	LINUX, WINDOWS, MACOS, NOT_DETECTED;

	protected static final Logger logger = LogManager.getLogger();

	public static EnvironmentOptions getFromSO() {
		String osName = System.getProperty("os.name").toLowerCase().substring(0, 3);
		logger.trace(".getFromSO [SO detected: {}]", osName);
		EnvironmentOptions _return;
		switch (osName) {
		case "mac":
			_return = MACOS;
			break;
		case "lin":
			_return = LINUX;
			break;
		case "win":
			_return = WINDOWS;
			break;
		default:
			_return = NOT_DETECTED;
		}
		logger.trace(".getFromSO [return: {}]", _return);
		return _return;
	}
	
}
