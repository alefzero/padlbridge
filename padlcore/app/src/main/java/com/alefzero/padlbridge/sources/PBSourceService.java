package com.alefzero.padlbridge.sources;

import java.util.Iterator;

import com.alefzero.padlbridge.config.model.SourceConfig;
import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.orchestrator.PBGenericService;

/**
 * General interface for data sources
 * 
 * @author xandecelo
 */
public abstract class PBSourceService extends PBGenericService<SourceConfig> {

	public abstract Iterator<DataEntry> getAllEntries();

}
