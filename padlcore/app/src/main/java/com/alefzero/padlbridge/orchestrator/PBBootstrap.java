package com.alefzero.padlbridge.orchestrator;

import java.util.HashMap;
import java.util.Map;
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
public class PBBootstrap {

	protected static final Logger logger = LogManager.getLogger();

	private Map<String, PBGenericService> targets = new HashMap<String, PBGenericService>();
	private Map<String, PBGenericService> sources = new HashMap<String, PBGenericService>();

	public PBBootstrap() {
		super();
	}

	public static void init() {
		PBBootstrap bootstraper = new PBBootstrap();
		bootstraper.loadServices(bootstraper.targets, PBTargetFactory.class);
		bootstraper.loadServices(bootstraper.sources, PBSourceFactory.class);
	}

	private void loadServices(Map<String, ? extends PBGenericService> serviceCollection,
			Class<? extends PBGenericFactory> factoryType) {

		logger.trace(".loadServices [serviceCollection: {}, factoryType {}]", serviceCollection, factoryType);
		ServiceLoader<? extends PBGenericFactory> serviceFactories = ServiceLoader.load(factoryType);

		for (PBGenericFactory serviceFactory : serviceFactories) {

			PBGenericService service = serviceFactory.getInstance();
			PBGenericService serviceWithSameID = targets.putIfAbsent(serviceFactory.getServiceId(), service);
			logger.trace("Adding service {} (id: {}) to items[{}].", service.getClass().getSimpleName(),
					serviceFactory.getServiceId(), serviceCollection);

			if (serviceWithSameID != null) {
				throw new PadlUnrecoverableError(String.format(
						PInfo.msg("orchestrator.bootstrap.serviceIdAlreadyInUse"), service.getClass().getSimpleName(),
						serviceFactory.getServiceId(), serviceWithSameID.getClass().getSimpleName()));
			}

		}
		logger.trace(".loadServices [return]");
	}

}
