package com.alefzero.padlbridge;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.core.Orchestrator;

/**
 * Padl Bridge Entry pointÏ
 * 
 * @author xandecelo
 *
 */
public class App {
	protected static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		String action = args.length > 0 ? args[0] : "help";
		String configurationFilename = getConfigurationFilename(args);
		new App().run(action, configurationFilename);
	}

	public static String getConfigurationFilename(String[] args) {
		logger.trace(".getConfigurationFilename [args: {}]", Arrays.toString(args));
		String configurationFilename = args.length > 1 ? args[1] : "";
		configurationFilename = configurationFilename.isEmpty() ? "./padlbridge.yaml" : configurationFilename;
		logger.trace(".getConfigurationFilename [return: {}]", configurationFilename);
		return configurationFilename;
	}

	private void run(String action, String configurationFilename) {
		logger.trace(".process [action: {}, configurationFilename: {}]", action, configurationFilename);
		Orchestrator.bootstrap(action, configurationFilename);
	}
}
