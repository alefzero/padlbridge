package com.alefzero.padlbridge.orchestrator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
import com.alefzero.padlbridge.sources.PBSourceFactory;
import com.alefzero.padlbridge.targets.PBTargetFactory;
import com.alefzero.padlbridge.util.PInfo;

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

	public PBServiceManager() {
		super();
		if (!initialized) {
			init();
		}
	}

	private void init() {
		loadServices(targetFactories, PBTargetFactory.class);
		loadServices(sourceFactories, PBSourceFactory.class);
		initialized = true;
	}

	private <T extends PBGenericFactory> void loadServices(Map<String, T> factoryCollection, Class<T> classType) {

		logger.trace(".loadServices [serviceCollection: {}, factoryType {}]", factoryCollection, classType);

		ServiceLoader<T> serviceFactories = ServiceLoader.load(classType);

		serviceFactories.forEach(serviceFactory -> {
			logger.trace("Loading component {} (id: {}) to items[{}].", serviceFactory.getClass().getSimpleName(),
					serviceFactory.getServiceType(), factoryCollection);

			var serviceFactoryWithSameType = factoryCollection.putIfAbsent(serviceFactory.getServiceType(),
					serviceFactory);

			if (serviceFactoryWithSameType != null) {
				throw new PadlUnrecoverableError(
						String.format(PInfo.msg("orchestrator.bootstrap.serviceIdAlreadyInUse"),
								serviceFactory.getClass().getSimpleName(), serviceFactory.getServiceType(),
								serviceFactoryWithSameType.getClass().getSimpleName()));
			}

		});

		logger.trace(".loadServices [return]");
	}

	public PBTargetFactory getTargetById(String targetFactoryId) {
		return Objects.requireNonNull(targetFactories.get(targetFactoryId), String
				.format("Service type %s found in configuration isn't available at this runtime.", targetFactoryId));
	}

	public PBSourceFactory getSourceById(String sourceFactoryId) {
		return Objects.requireNonNull(sourceFactories.get(sourceFactoryId), String
				.format("Service type %s found in configuration isn't available at this runtime.", sourceFactoryId));
	}

}
