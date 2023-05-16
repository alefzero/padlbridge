package com.alefzero.padlbridge.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.alefzero.padlbridge.config.model.CacheConfig;
import com.alefzero.padlbridge.orchestrator.PBGenericService;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.alefzero.padlbridge.targets.PBTargetService;

public abstract class PBCacheService extends PBGenericService<CacheConfig> {

	public static final int CACHED_ENTRY_STATUS_UNSET = 0;
	public static final int CACHED_ENTRY_STATUS_EXISTS = 1;
	public static final int CACHED_ENTRY_STATUS_DELETE = 2;
	public static final int CACHED_ENTRY_STATUS_ADD = 3;
	public static final int CACHED_ENTRY_STATUS_UPDATE = 4;
	public static final int CACHED_ENTRY_STATUS_DO_NOTHING = 5;

	/**
	 * Set the cache service to initial state and prepare its resources for the next
	 * sync operation. This method is automatically called by
	 * {@link #sync(PBTargetService, List)} when a sync operation between sources
	 * and a target is requested. Can be called manually for a reset of cache
	 * resources.
	 */
	public abstract void prepare();

	public void sync(PBTargetService target, List<PBSourceService> sources) {

		this.prepare();

		List<PBSourceService> sourcesInReverseOrder = new ArrayList<PBSourceService>(sources);
		Collections.reverse(sourcesInReverseOrder);

		sourcesInReverseOrder.forEach(source -> {
			setEntryUidAsFoundFor(source.getName(), source.getAllUids());
		});

	}

	/**
	 * Return a iterator of all DNs presents at the cache but not present in the
	 * source.
	 * 
	 * @param source
	 * @return
	 */
	protected abstract Iterator<String> getDeletedUidsFrom(String sourceName);

	/**
	 * Set status of a list of uid in the cache as found at the source.
	 * 
	 * @param sourceName
	 * 
	 * @param source
	 * @param dn
	 */
	protected abstract void setEntryUidAsFoundFor(String sourceName, Iterator<String> allDistinctUids);

	/**
	 * Remove an DN from cache
	 * 
	 * @param sourceName
	 * 
	 * @param source
	 * @param dn
	 */
	protected abstract void removeDNFromCache(String sourceName, String dn);

	/**
	 * Check and return the operation to be executed for this uid entry at the
	 * target, based on source data.
	 * 
	 * @param soourceName
	 * @param uid
	 * @param hash
	 * @return
	 */
	protected abstract int getExpectedOperationFor(String sourceName, String uid, String hash);

	/**
	 * Update hash entry for this cache. Used to sync the result operation after
	 * target has been update.
	 * 
	 * @param cacheOperationValue if is ADD or UPDATE
	 * @param sourceName
	 * @param uid
	 * @param dn
	 * @param hash
	 * @return
	 */
	protected abstract void updateCacheWithData(int cacheOperationValue, String sourceName, String uid, String dn,
			String hash);

}