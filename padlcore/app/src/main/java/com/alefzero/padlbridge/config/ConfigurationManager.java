package com.alefzero.padlbridge.config;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.config.model.GeneralConfig;
import com.alefzero.padlbridge.config.model.TargetLDAPConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigurationManager {

	protected static final Logger logger = LogManager.getLogger();

	public ConfigurationManager(Path configurationFile) {
		super();
		loadFile(configurationFile);
	}

	private void loadFile(Path configurationFile) {
		logger.trace(".loadFile [configurationFile: {}]", configurationFile);
		try {
			// TODO implement acording with new YAML
			
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			JsonNode rootTree = mapper.readTree(configurationFile.toFile());
			GeneralConfig config = mapper.treeToValue(rootTree.get("general"), GeneralConfig.class);
			logger.debug("Config data: {}", config);
			
			rootTree.get("sources").forEach(source -> {
				logger.debug("Source found: {}", source);
			});
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public TargetLDAPConfig getTargetLDAPConfig() {
		return null;
	}

}