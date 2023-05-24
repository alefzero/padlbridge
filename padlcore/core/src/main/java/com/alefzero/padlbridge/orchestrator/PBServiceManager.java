package com.alefzero.padlbridge.orchestrator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.cache.PBCacheFactory;
import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.config.model.CacheConfig;
import com.alefzero.padlbridge.config.model.InstanceConfig;
import com.alefzero.padlbridge.config.model.SourceConfig;
import com.alefzero.padlbridge.config.model.TargetConfig;
import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
import com.alefzero.padlbridge.sources.PBSourceFactory;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.alefzero.padlbridge.targets.PBTargetFactory;
import com.alefzero.padlbridge.targets.PBTargetService;
import com.alefzero.padlbridge.util.PInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Load and register services and components
 * 
 * @author xandecelo
 */
public class PBServiceManager {

	protected static final Logger logger = LogManager.getLogger();

	private static boolean initialized = false;

	private static Map<String, PBTargetFactory> targetFactories = new HashMap<String, PBTargetFactory>();
	private static Map<String, PBSourceFactory> sourceFactories = new HashMap<String, PBSourceFactory>();
	private static Map<String, PBCacheFactory> cacheFactories = new HashMap<String, PBCacheFactory>();
	private static PBLoadedServices services = null;

	public PBServiceManager(Path configurationFilePath) {
		super();
		if (!initialized) {
			init(configurationFilePath);
		}
	}

	private void init(Path configurationFilePath) {

		if (!Files.exists(configurationFilePath)) {
			String message = String.format(PInfo.log("config.error.configurationFileNotFound"), configurationFilePath,
					configurationFilePath.toFile().getAbsolutePath());
			throw new PadlUnrecoverableError(message);
		}

		loadServiceFactories(targetFactories, PBTargetFactory.class);
		loadServiceFactories(sourceFactories, PBSourceFactory.class);
		loadServiceFactories(cacheFactories, PBCacheFactory.class);

		loadServices(configurationFilePath);

		initialized = true;

	}

	private <T extends PBGenericFactory> void loadServiceFactories(Map<String, T> factoryCollection,
			Class<T> classType) {

		logger.trace(".loadServices [serviceCollection: {}, factoryType {}]", factoryCollection, classType);

		ServiceLoader<T> serviceFactories = ServiceLoader.load(classType);

		serviceFactories.forEach(serviceFactory -> {
			logger.debug("Loading component {} (id: {}) to items[{}].", serviceFactory.getClass().getSimpleName(),
					serviceFactory.getServiceType(), factoryCollection);

			var serviceFactoryWithSameType = factoryCollection.putIfAbsent(serviceFactory.getServiceType(),
					serviceFactory);

			if (serviceFactoryWithSameType != null) {
				throw new PadlUnrecoverableError(PInfo.msg("orchestrator.bootstrap.serviceIdAlreadyInUse",
						serviceFactory.getClass().getSimpleName(), serviceFactory.getServiceType(),
						serviceFactoryWithSameType.getClass().getSimpleName()));
			}

		});

		logger.trace(".loadServices [return]");
	}

	private void loadServices(Path configurationFilePath) {
		logger.trace(".parseConfig [configurationFile: {}]", configurationFilePath);

		try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			JsonNode rootTree = mapper.readTree(configurationFilePath.toFile());

			InstanceConfig instanceConfig = mapper.treeToValue(rootTree.get("general"), InstanceConfig.class);

			PBCacheFactory cacheFactory = this.getCacheFactoryByType(rootTree.get("cache").get("type").asText());
			CacheConfig cacheConfig = mapper.treeToValue(rootTree.get("cache"), cacheFactory.getConfigClass());
			PBCacheService cacheService = cacheFactory.getInstance();
			cacheConfig.checkConfiguration();
			cacheService.setConfig(cacheConfig);

			PBTargetFactory targetFactory = getTargetFactoryByType(rootTree.get("target").get("type").asText());
			TargetConfig targetConfig = mapper.treeToValue(rootTree.get("target"), targetFactory.getConfigClass());
			targetConfig.checkConfiguration();
			PBTargetService targetService = targetFactory.getInstance();
			targetService.setConfig(targetConfig);

			List<PBSourceService> sourceServices = new ArrayList<PBSourceService>();

			for (JsonNode source : rootTree.get("sources")) {

				PBSourceFactory sourceFactory = getSourceFactoryByType(source.get("type").asText());
				SourceConfig sourceConfig = mapper.treeToValue(source, sourceFactory.getConfigClass());
				sourceConfig.checkConfiguration();
				PBSourceService sourceService = sourceFactory.getInstance();
				sourceService.setConfig(sourceConfig);

				sourceServices.add(sourceService);
			}

			services = new PBLoadedServices(instanceConfig, cacheService, targetService, sourceServices);

		} catch (IOException e) {
			e.printStackTrace();
			throw new PadlUnrecoverableError(e.getCause());
		}

	}

	private PBTargetFactory getTargetFactoryByType(String targetFactoryType) {
		return Objects.requireNonNull(targetFactories.get(targetFactoryType),
				String.format(PInfo.msg("orchestrator.serviceManager.componentNotFound"), targetFactoryType));
	}

	private PBSourceFactory getSourceFactoryByType(String sourceFactoryType) {
		return Objects.requireNonNull(sourceFactories.get(sourceFactoryType),
				String.format(PInfo.msg("orchestrator.serviceManager.componentNotFound"), sourceFactoryType));
	}

	private PBCacheFactory getCacheFactoryByType(String cacheFactoryType) {
		return Objects.requireNonNull(cacheFactories.get(cacheFactoryType),
				String.format(PInfo.msg("orchestrator.serviceManager.componentNotFound"), cacheFactoryType));
	}

	public PBLoadedServices getServices() {
		return services;
	}

}
