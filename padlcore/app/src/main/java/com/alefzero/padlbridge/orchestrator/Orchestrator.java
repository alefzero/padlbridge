package com.alefzero.padlbridge.orchestrator;


import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.config.ConfigurationFactory;
import com.alefzero.padlbridge.config.ConfigurationManager;
import com.alefzero.padlbridge.util.PInfo;

/**
 * Bootstrap and control the application actions
 * 
 * @author xandecelo
 *
 */
public class Orchestrator {
	protected static final Logger logger = LogManager.getLogger();

	// PadlBridge components
	private ConfigurationManager configManager = null;

	public Orchestrator() {
		super();
	}

	public void bootstrap(String action, String configurationFilename) {
		logger.trace(".bootstrap [action: {}, configurationFilename: {}]", action, configurationFilename);

		this.configureEnvironment(configurationFilename);

		switch (action.toLowerCase()) {
		case "getenv":
			this.getEnvironmentVariablesFromYAML(EnvironmentOptions.getFromSO());
			break;
		case "load":
			break;
		case "update":
			break;
		case "keepInSync":
			break;
		case "help":
		default:
			this.help();
			break;
		}
	}

	public void configureEnvironment(String configurationFilename) {
		logger.trace(".configureEnvironment [configurationFilename: {}]", configurationFilename);
		configManager = ConfigurationFactory.getInstance(Paths.get(configurationFilename));
		logger.trace(PInfo.log("orchestrator.configureEnvironment.targetConfigLoaded"), configManager.getTargetLDAPConfig());
	}

	public void getEnvironmentVariablesFromYAML(EnvironmentOptions osSyntax) {
		logger.trace(".getEnvironmentVariablesFromYAML [osSyntax: {}]", osSyntax);
	}

	private void help() {
		logger.trace(".help");
		System.out.println(PInfo.msg("app.welcome-version"));
		System.out.println(PInfo.msg("app.help"));
	}
}
