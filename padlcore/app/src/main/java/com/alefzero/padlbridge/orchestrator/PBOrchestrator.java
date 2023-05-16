package com.alefzero.padlbridge.orchestrator;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.alefzero.padlbridge.targets.PBTargetService;

/**
 * Bootstrap and control the application actions
 * 
 * @author xandecelo
 *
 */
public class PBOrchestrator {
	protected static final Logger logger = LogManager.getLogger();

	private PBCacheService cache;
	private PBTargetService target;
	private List<PBSourceService> sources;

	public PBOrchestrator(PBLoadedServices services) {
		super();
		cache = services.getCache();
		target = services.getTarget();
		sources = services.getSources();
		
	}

	public void sync() {
		logger.trace(".sync");
		cache.sync(target, sources);
	}
}
