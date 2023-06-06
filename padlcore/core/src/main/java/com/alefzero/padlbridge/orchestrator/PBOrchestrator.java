package com.alefzero.padlbridge.orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.config.model.OperationalActions;
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
		logger.debug(PInfo.log("orchestrator.initAction"));
		this.cache = services.getCache();
		this.target = services.getTarget();
		this.sources = services.getSources();
		sourcesInReverseOrder = new ArrayList<PBSourceService>(sources);
		Collections.reverse(sourcesInReverseOrder);
	}

	public void sync() {
		logger.trace(".sync");
		cache.prepare();
		try {

			for (PBSourceService source : sourcesInReverseOrder) {
				logger.debug(PInfo.log("orchestrator.check-items-to-delete", source.getName()));
				cache.syncUidsFromSource(source.getName(), source.getAllUids());
				Deque<String> deletedDNs = target.deleteAll(cache.getAllDNsToBeDeletedFromSource(source.getName()));
				cache.removeFromCacheByDN(source.getName(), deletedDNs);
			}

			for (PBSourceService source : sources) {
				logger.debug(PInfo.log("orchestrator.check-items-to-add-or-modify", source.getName()));

				Iterator<DataEntry> iterator = source.getAllEntries();

				OperationalActions cacheDefaultAddOperation = cache
						.getBehaviourForAddingNewEntries(source.getConfig().getDefaultOperation());

				while (iterator.hasNext()) {

					DataEntry dataEntry = iterator.next();
					OperationalActions action = cache.getExpectedOperationFor(cacheDefaultAddOperation,
							source.getName(), dataEntry.getUid(), dataEntry.getHash());

					switch (action) {
					case UNSET:
					case EXISTS:
					case DO_NOTHING:
						break;
					case REPLACE:
						// REPLACE = DELETE + ADD
						target.delete(dataEntry.getEntry());
					case ADD:
						target.add(dataEntry.getEntry());
						cache.updateCacheWithData(action, source.getName(), dataEntry.getUid(),
								dataEntry.getEntry().getDN(), dataEntry.getHash());
						break;
					case UPDATE:
						target.modify(dataEntry.getEntry());
						cache.updateCacheWithData(action, source.getName(), dataEntry.getUid(),
								dataEntry.getEntry().getDN(), dataEntry.getHash());
						break;
					case DELETE:
						// treated by prior loop phase
						break;
					default:
						logger.error("Error processing data: operation {} for DN: {}", action,
								dataEntry.getEntry().getDN());
						break;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(PInfo.msg("orchestrator.source-error-try-next"));
		}

		logger.info(PInfo.msg("orchestrator.waiting-next-process"));

	}
}

// Note

//	maybe
//Spliterator<DataEntry> split = Spliterators.spliteratorUnknownSize(source.getAllEntries(), 0);
//StreamSupport.stream(split, true).parallel().forEach(dataEntry -> {

//source.getAllEntries().forEachRemaining(dataEntry -> {
//	int operation = cache.getExpectedOperationFor(source.getName(), dataEntry.getUid(),
//			dataEntry.getHash());
//	if (PBCacheService.CACHED_ENTRY_STATUS_DO_NOTHING == operation) {
//		// DO NOTHING
//	} else if (PBCacheService.CACHED_ENTRY_STATUS_UPDATE == operation
//			|| "update".equalsIgnoreCase(source.getConfig().getDefaultOperation())) {
//		target.modify(dataEntry.getEntry());
//		cache.updateCacheWithData(operation, source.getName(), dataEntry.getUid(),
//				dataEntry.getEntry().getDN(), dataEntry.getHash());
//	} else if (PBCacheService.CACHED_ENTRY_STATUS_ADD == operation) {
//		target.add(dataEntry.getEntry());
//		cache.updateCacheWithData(operation, source.getName(), dataEntry.getUid(),
//				dataEntry.getEntry().getDN(), dataEntry.getHash());
//	} else {
//		logger.error("Error processing data: operation {} for DN: {}", operation,
//				dataEntry.getEntry().getDN());
//	}
//});
