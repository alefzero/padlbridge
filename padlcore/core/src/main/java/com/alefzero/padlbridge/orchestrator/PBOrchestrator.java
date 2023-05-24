package com.alefzero.padlbridge.orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.alefzero.padlbridge.targets.PBTargetService;
import com.alefzero.padlbridge.util.PInfo;

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
		logger.debug("orchestrator.initAction");

		cache.prepare();

		sourcesInReverseOrder.forEach(source -> {
			logger.debug("Checking for removed items for source {}", source.getName());
			cache.syncUidsFromSource(source.getName(), source.getAllUids());
			Deque<String> deletedDNs = target.deleteAll(cache.getAllDNsToBeDeletedFromSource(source.getName()));
			cache.removeFromCacheByDN(source.getName(), deletedDNs);
		});

		sources.forEach(source -> {
			logger.debug("Checking for items to add/modify at source {}", source.getName());
// 			maybe
//			Spliterator<DataEntry> split = Spliterators.spliteratorUnknownSize(source.getAllEntries(), 0);
//			StreamSupport.stream(split, true).parallel().forEach(dataEntry -> {
			
			Iterator<DataEntry> iterator = source.getAllEntries();
			while (iterator.hasNext()) {
				DataEntry dataEntry = iterator.next();
				int operation = cache.getExpectedOperationFor(source.getName(), dataEntry.getUid(),
						dataEntry.getHash());
				if (PBCacheService.CACHED_ENTRY_STATUS_DO_NOTHING == operation) {
					// DO NOTHING
				} else if (PBCacheService.CACHED_ENTRY_STATUS_UPDATE == operation
						|| "update".equalsIgnoreCase(source.getConfig().getDefaultOperation())) {
					target.modify(dataEntry.getEntry());
					cache.updateCacheWithData(operation, source.getName(), dataEntry.getUid(),
							dataEntry.getEntry().getDN(), dataEntry.getHash());
				} else if (PBCacheService.CACHED_ENTRY_STATUS_ADD == operation) {
					target.add(dataEntry.getEntry());
					cache.updateCacheWithData(operation, source.getName(), dataEntry.getUid(),
							dataEntry.getEntry().getDN(), dataEntry.getHash());
				} else {
					logger.error("Error processing data: operation {} for DN: {}", operation,
							dataEntry.getEntry().getDN());
				}
			}
		
//			source.getAllEntries().forEachRemaining(dataEntry -> {
//				int operation = cache.getExpectedOperationFor(source.getName(), dataEntry.getUid(),
//						dataEntry.getHash());
//				if (PBCacheService.CACHED_ENTRY_STATUS_DO_NOTHING == operation) {
//					// DO NOTHING
//				} else if (PBCacheService.CACHED_ENTRY_STATUS_UPDATE == operation
//						|| "update".equalsIgnoreCase(source.getConfig().getDefaultOperation())) {
//					target.modify(dataEntry.getEntry());
//					cache.updateCacheWithData(operation, source.getName(), dataEntry.getUid(),
//							dataEntry.getEntry().getDN(), dataEntry.getHash());
//				} else if (PBCacheService.CACHED_ENTRY_STATUS_ADD == operation) {
//					target.add(dataEntry.getEntry());
//					cache.updateCacheWithData(operation, source.getName(), dataEntry.getUid(),
//							dataEntry.getEntry().getDN(), dataEntry.getHash());
//				} else {
//					logger.error("Error processing data: operation {} for DN: {}", operation,
//							dataEntry.getEntry().getDN());
//				}
//			});
		});
		
		logger.info(PInfo.msg("orchestrator.waiting-next-process"));

	}
}
