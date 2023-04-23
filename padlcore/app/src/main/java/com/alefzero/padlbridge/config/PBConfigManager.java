package com.alefzero.padlbridge.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.config.model.GeneralConfig;
import com.alefzero.padlbridge.config.model.SourceConfig;
import com.alefzero.padlbridge.config.model.TargetConfig;
import com.alefzero.padlbridge.config.model.UserConfig;
import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
import com.alefzero.padlbridge.orchestrator.PBServiceManager;
import com.alefzero.padlbridge.sources.PBSourceFactory;
import com.alefzero.padlbridge.targets.PBTargetFactory;
import com.alefzero.padlbridge.util.PInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class PBConfigManager {
	protected static final Logger logger = LogManager.getLogger();

	public static UserConfig getConfigurationFor(Path configurationFile, PBServiceManager serviceManager) {
		logger.trace(".getConfigurationFor [configurationFilename: {}, serviceManager: {}]", configurationFile,
				serviceManager);

		if (!Files.exists(configurationFile)) {
			String message = String.format(PInfo.log("config.error.configurationFileNotFound"), configurationFile,
					configurationFile.toFile().getAbsolutePath());
			throw new PadlUnrecoverableError(message);
		}

		UserConfig userConfig = parseConfig(configurationFile, serviceManager);

//		logger.trace(PInfo.log("orchestrator.configureEnvironment.targetConfigLoaded"),
//				configManager.getTargetLDAPConfig());

		return userConfig;
	}

	private static UserConfig parseConfig(Path configurationFile, PBServiceManager serviceManager) {
		logger.trace(".parseConfig [configurationFile: {}]", configurationFile);

		GeneralConfig generalConfig = null;
		TargetConfig targetConfig = null;
		List<SourceConfig> sourceConfigList = new LinkedList<SourceConfig>();

		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			JsonNode rootTree = mapper.readTree(configurationFile.toFile());
			generalConfig = mapper.treeToValue(rootTree.get("general"), GeneralConfig.class);

			PBTargetFactory targetFactory = serviceManager.getTargetById(rootTree.get("target").get("type").asText());

			targetConfig = mapper.treeToValue(rootTree.get("target"), targetFactory.getConfigClass());

			for (JsonNode source : rootTree.get("sources")) {
				PBSourceFactory sourceFactory = serviceManager.getSourceById(source.get("type").asText());
				SourceConfig sourceConfig = mapper.treeToValue(source, sourceFactory.getConfigClass());
				sourceConfigList.add(sourceConfig);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		UserConfig userConfig = new UserConfig(generalConfig, targetConfig, sourceConfigList);

		System.out.println(userConfig);
		
		return userConfig;
	}

}
