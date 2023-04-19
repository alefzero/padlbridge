package com.alefzero.padlbridge.config;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
import com.alefzero.padlbridge.util.PInfo;

public class ConfigurationFactory {
	protected static final Logger logger = LogManager.getLogger();

	private ConfigurationFactory() {
		super();
	}

	public static ConfigurationManager getInstance(Path configurationFile) {
		logger.trace(".getInstance [configurationFile: {}]", configurationFile);

		if (!Files.exists(configurationFile)) {
			String message = String.format(PInfo.log("config.error.configurationFileNotFound"), configurationFile,
					configurationFile.toFile().getAbsolutePath());
			throw new PadlUnrecoverableError(message);
		}
				
		ConfigurationManager manager = new ConfigurationManager(configurationFile);
		return manager ;
	}

}
