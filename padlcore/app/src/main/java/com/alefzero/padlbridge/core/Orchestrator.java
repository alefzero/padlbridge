package com.alefzero.padlbridge.core;

import static com.alefzero.padlbridge.util.MessageService.log;
import static com.alefzero.padlbridge.util.MessageService.msg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bootstrap and control the application actions
 * 
 * @author xandecelo
 *
 */
public class Orchestrator {
	protected static final Logger logger = LogManager.getLogger();

	private static Orchestrator orchestrator = new Orchestrator();

	private Orchestrator() {
		super();
	}

	public void configureEnvironment() {
		// MessageService.setLocale(new Locale("pt", "BR"));
	}

	public static void bootstrap(String action, String configurationFilename) {
		logger.trace(".bootstrap [action: {}, configurationFilename: {}]", action, configurationFilename);
		logger.trace(log("orchestrator.initAction"));
		orchestrator.configureEnvironment();
		
		switch (action.toLowerCase()) {
		case "config":
			orchestrator.configureEnvironment();
			break;
		case "getenv":
			orchestrator.getEnvironmentVariables(EnvironmentOptions.getFromSO());
			break;
		case "load":
			break;
		case "update":
			break;
		case "keepInSync":
			break;
		case "help":
		default:
			orchestrator.help();
			break;
		}
	}

	public void getEnvironmentVariables(EnvironmentOptions osSyntax) {
		logger.trace(".getEnvironmentVariables [osSyntax: {}]", osSyntax);

	}

	private void help() {
		logger.trace(".help");
		System.out.println(msg("app.welcome-version"));
		System.out.println(msg("app.help"));
	}
}
