package com.alefzero.padlbridge.orchestrator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.util.PInfo;

/**
 * Bootstrap and control the application actions
 * 
 * @author xandecelo
 *
 */
public class PBOrchestrator {
	protected static final Logger logger = LogManager.getLogger();

	public PBOrchestrator() {
		super();
	}

	public void bootstrap(String action, String configurationFilename, PBServiceManager serviceManager) {
		logger.trace(".bootstrap [action: {}, configurationFilename: {}]", action, configurationFilename);

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

	public void getEnvironmentVariablesFromYAML(EnvironmentOptions osSyntax) {
		logger.trace(".getEnvironmentVariablesFromYAML [osSyntax: {}]", osSyntax);
	}

	private void help() {
		logger.trace(".help");
		System.out.println(PInfo.msg("app.welcome-version"));
		System.out.println(PInfo.msg("app.help"));
	}
}
