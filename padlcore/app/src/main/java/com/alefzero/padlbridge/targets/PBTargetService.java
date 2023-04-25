package com.alefzero.padlbridge.targets;

import java.util.Iterator;

import com.alefzero.padlbridge.config.model.TargetConfig;
import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.orchestrator.PBGenericService;

/**
 * General interface for ldap targets
 * 
 * @author xandecelo
 *
 */
public abstract class PBTargetService extends PBGenericService<TargetConfig> {

	public abstract void deleteAll(Iterator<String> listOfDeletedDN);

	public abstract void addAll(Iterator<DataEntry> entriesToAddFrom);

	public abstract void modifyAll(Iterator<DataEntry> entriesToModifyFrom);

}
