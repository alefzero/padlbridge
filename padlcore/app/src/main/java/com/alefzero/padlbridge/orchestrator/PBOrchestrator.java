package com.alefzero.padlbridge.orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
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
		this.cache = services.getCache();
		this.target = services.getTarget();
		this.sources = services.getSources();
		sourcesInReverseOrder = new ArrayList<PBSourceService>(sources);
		Collections.reverse(sourcesInReverseOrder);
	}

	public void sync() {
		logger.trace(".sync");

		cache.prepare();

		sourcesInReverseOrder.forEach(source -> {
			cache.syncUidsFromSource(source.getName(), source.getAllUids());
			Deque<String> deletedDNs = target.deleteAll(cache.getAllDNsToBeDeletedFromSource(source.getName()));
			cache.removeFromCacheByDN(source.getName(), deletedDNs);
		});

		sources.forEach(source -> {
			// maybe .parallelStream()
			source.getAllEntries().forEachRemaining(dataEntry -> {
				int operation = cache.getExpectedOperationFor(source.getName(), dataEntry.getUid(),
						dataEntry.getHash());
				if (PBCacheService.CACHED_ENTRY_STATUS_ADD == operation) {
					target.add(dataEntry.getEntry());
				} else if (PBCacheService.CACHED_ENTRY_STATUS_UPDATE == operation) {
					target.modify(dataEntry.getEntry());
				} else { 
					logger.error("Error processing data: operation {} for DN: {}", operation, dataEntry.getEntry().getDN());
				}
			});
		});

	}
}
