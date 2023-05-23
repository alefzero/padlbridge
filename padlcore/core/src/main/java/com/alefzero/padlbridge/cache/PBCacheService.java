package com.alefzero.padlbridge.cache;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.config.model.CacheConfig;
import com.alefzero.padlbridge.exceptions.PadlRecoverableException;
import com.alefzero.padlbridge.orchestrator.PBGenericService;
import com.alefzero.padlbridge.targets.PBTargetService;
import com.alefzero.padlbridge.util.PInfo;

public abstract class PBCacheService extends PBGenericService<CacheConfig> {

	protected static final Logger logger = LogManager.getLogger();

	public static final int CACHED_ENTRY_STATUS_UNSET = 0;
	public static final int CACHED_ENTRY_STATUS_EXISTS = 1;
	public static final int CACHED_ENTRY_STATUS_DELETE = 2;
	public static final int CACHED_ENTRY_STATUS_ADD = 3;
	public static final int CACHED_ENTRY_STATUS_UPDATE = 4;
	public static final int CACHED_ENTRY_STATUS_DO_NOTHING = 5;

	private String instanceName = "default";

	/**
	 * Return the instance which this cache is used for. If no instance name is set,
	 * the name "default" is used.
	 * 
	 * @return
	 */
	public String getInstanceName() {
		logger.trace(PInfo.log("cache.get-instance"));
		return instanceName;
	}

	/**
	 * Set the instance name this cache to use. If no instance name is set, the name
	 * "default" is used. Only letters, numbers and _ symbol are allowed
	 * 
	 * @param instanceName
	 */
	public void setInstanceName(String instanceName) {
		logger.trace(".setInstanceName [{}]", instanceName);

		if (Objects.requireNonNull(instanceName).matches("[a-zA-Z0-9_]*") && instanceName.length() <= 30
				&& !instanceName.isBlank()) {
			this.instanceName = instanceName;
		} else {
			throw new PadlRecoverableException(PInfo.msg("cache.invalid-instance-name", instanceName));
		}
	}

	/**
	 * Set the cache service to initial state and prepare its resources for the next
	 * sync operation. This method is automatically called by
	 * {@link #sync(PBTargetService, List)} when a sync operation between sources
	 * and a target is requested. Can be called manually for a reset of cache
	 * resources.
	 */
	public abstract void prepare();

	/**
	 * Return a iterator of all DNs presents at the cache but not present in the
	 * source.
	 * 
	 * @param source
	 * @return
	 */
	public abstract Iterator<String> getAllDNsToBeDeletedFromSource(String sourceName);

	/**
	 * Set status of a list of uid in the cache as found at the source.
	 * 
	 * @param sourceName
	 * 
	 * @param source
	 * @param dn
	 */

	public abstract void syncUidsFromSource(String sourceName, Iterator<String> allDistinctUids);

	/**
	 * Remove an DN from cache
	 * 
	 * @param sourceName
	 * 
	 * @param source
	 * @param dn
	 */
	public abstract void removeFromCacheByDN(String sourceName, Deque<String> dnItems);

	/**
	 * Check and return the operation to be executed for this uid entry at the
	 * target, based on source data.
	 * 
	 * @param soourceName
	 * @param uid
	 * @param hash
	 * @return
	 */
	public abstract int getExpectedOperationFor(String sourceName, String uid, String hash);

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
	public abstract void updateCacheWithData(int cacheOperationValue, String sourceName, String uid, String dn,
			String hash);

}