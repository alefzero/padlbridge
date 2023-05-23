package com.alefzero.padlbridge;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
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
		logger.info(PInfo.msg("app.welcome-version"));
		logger.debug(PInfo.log("app.argument-list", args));

		String action = args.length > 0 ? args[0] : "help";
		String configurationFilename = getConfigurationFilename(args);
		new App().run(action, configurationFilename);
	}

	private ScheduledFuture<?> executor = null;

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
		if (padlLang != null) {
			Locale.setDefault(new Locale(padlLang));
			PInfo.setLocale(Locale.getDefault());
			logger.debug(PInfo.log("app.lang-variable-setup", padlLang));
		}
	}

	private void help() {
		logger.trace(".help");
		logger.info(PInfo.msg("app.help"));
	}

	private void runSyncProcess(String configurationFilename) {
		logger.trace(".runSyncProcess [configurationFilename: {}]", configurationFilename);

		Thread shutdownListener = new Thread() {
			public void run() {
				logger.info(PInfo.msg("app.shutdown-requested"));
				try {
					if (executor != null) {
						executor.cancel(false);
						Thread.sleep(15000);
					}
					logger.info(PInfo.msg("app.shutdown"));
				} catch (InterruptedException e) {
					logger.error(PInfo.msg("app.aborting"));
				}
			}
		};

		Runtime.getRuntime().addShutdownHook(shutdownListener);

		try {
			PBServiceManager serviceManager = new PBServiceManager(Paths.get(configurationFilename));
			PBOrchestrator orchestrator = new PBOrchestrator(serviceManager.getServices());
			executor = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> orchestrator.sync(), 0,
					30, TimeUnit.SECONDS);
		} catch (PadlUnrecoverableError e) {
			logger.error(e);
		}
	}

}
