package com.alefzero.padlbridge.targets;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.config.model.TargetConfig;
import com.alefzero.padlbridge.orchestrator.PBGenericService;
import com.unboundid.ldap.sdk.Entry;

/**
 * General interface for ldap targets
 * 
 * @author xandecelo
 *
 */
public abstract class PBTargetService extends PBGenericService<TargetConfig> {
	
	protected static final Logger logger = LogManager.getLogger();

	public abstract void deleteAll(Iterator<String> listOfDeletedDN);

	public abstract void addAll(Iterator<Entry> entriesToAddFrom);

	public abstract void modifyAll(Iterator<Entry> entriesToModifyFrom);

}
