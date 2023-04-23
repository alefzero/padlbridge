package com.alefzero.padlbridge;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.config.PBConfigManager;
import com.alefzero.padlbridge.config.model.UserConfig;
import com.alefzero.padlbridge.orchestrator.PBOrchestrator;
import com.alefzero.padlbridge.orchestrator.PBServiceManager;

/**
 * Padl Bridge Entry point
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
		logger.trace(".run [action: {}, configurationFilename: {}]", action, configurationFilename);
		setupFromOSEnvironmentVariables();

		PBServiceManager serviceManager = new PBServiceManager();
		UserConfig userConfig = PBConfigManager.getConfigurationFor(Paths.get(configurationFilename), serviceManager);

		new PBOrchestrator().bootstrap(action, configurationFilename, serviceManager);
	}

	private void setupFromOSEnvironmentVariables() {
		logger.trace(".setupFromSOEnvironmentVariables");
		String padlLang = System.getenv("PADL_LANG");
		logger.trace("padlLang = {} ", padlLang);
		if (padlLang != null) {
			logger.debug("Setting language to {} per PADL_LANG variable from OS", padlLang);
			Locale.setDefault(new Locale(padlLang));
		}
	}
}
