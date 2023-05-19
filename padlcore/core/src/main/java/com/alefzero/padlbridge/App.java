package com.alefzero.padlbridge;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.orchestrator.PBOrchestrator;
import com.alefzero.padlbridge.orchestrator.PBServiceManager;
import com.alefzero.padlbridge.util.PInfo;

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

	private void run(String action, String configurationFilename) {
		logger.trace(".run [action: {}, configurationFilename: {}]", action, configurationFilename);
		setupFromOSEnvironmentVariables();

		switch (action.toLowerCase()) {
		case "run":
			runSyncProcess(configurationFilename);
			break;
		case "help":
		default:
			this.help();
			break;
		}
	}

	public static String getConfigurationFilename(String[] args) {
		logger.trace(".getConfigurationFilename [args: {}]", Arrays.toString(args));
		String configurationFilename = args.length > 1 ? args[1] : "";
		configurationFilename = configurationFilename.isEmpty() ? "./conf/padlbridge.yaml" : configurationFilename;
		logger.trace(".getConfigurationFilename [return: {}]", configurationFilename);
		return configurationFilename;
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

	private void help() {
		logger.trace(".help");
		System.out.println(PInfo.msg("app.welcome-version"));
		System.out.println(PInfo.msg("app.help"));
	}

	private void runSyncProcess(String configurationFilename) {
		logger.trace(".runSyncProcess [configurationFilename: {}]", configurationFilename);

		Thread shutdownListener = new Thread() {
			public void run() {
				logger.info("Shutdown requested. Waiting running process to end (max=10s).");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					logger.trace("Error while ending runing process. Aborting.");
				}
			}
		};

		Runtime.getRuntime().addShutdownHook(shutdownListener);

		PBServiceManager serviceManager = new PBServiceManager(Paths.get(configurationFilename));
		PBOrchestrator orchestrator = new PBOrchestrator(serviceManager.getServices());

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> orchestrator.sync(), 0, 30,
				TimeUnit.SECONDS);

	}

}
