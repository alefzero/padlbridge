package com.alefzero.padlbridge.cache;

import java.util.Iterator;

import com.alefzero.padlbridge.config.model.CacheConfig;
import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.orchestrator.PBGenericService;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.unboundid.ldap.sdk.Entry;

public abstract class PBCacheService extends PBGenericService<CacheConfig> {

	/**
	 * Set the cache service to the initial state
	 */
	public abstract void prepare();

	public abstract void addHashesFrom(PBSourceService source);

	public abstract Iterator<String> getDeletedEntriesFrom(PBSourceService source);

	public abstract void consolidate();

	public abstract Iterator<DataEntry> getEntriesToAddOrModify(PBSourceService source);

	public abstract void updateTables();

}
