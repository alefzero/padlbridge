package com.alefzero.padlbridge.orchestrator;

import java.util.ArrayList;
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
	private List<PBSourceService> sourcesInReverseOrder;

	public PBOrchestrator(PBLoadedServices services) {
		super();
		cache = services.getCache();
		target = services.getTarget();
		sources = services.getSources();
		sourcesInReverseOrder = new ArrayList<PBSourceService>();
		sources.forEach(source -> sourcesInReverseOrder.add(0, source));
	}

	public void sync() {

		cache.prepare();
		sources.forEach(source -> {
			cache.addHashesFrom(source);
		});

		cache.consolidate();

		sourcesInReverseOrder.forEach(source -> {
			target.deleteAll(cache.getDeletedEntriesFrom(source));
		});

		sources.forEach(source -> {
			target.addAll(cache.getEntriesToAddFrom(source));
			target.modifyAll(cache.getEntriesToModifyFrom(source));
		});
		
		cache.updateTables();
		
	}
}
