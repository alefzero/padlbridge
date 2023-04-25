package com.alefzero.padlbridge.orchestrator;

import java.util.List;

import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.config.model.InstanceConfig;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.alefzero.padlbridge.targets.PBTargetService;

public class PBLoadedServices {

	private InstanceConfig instanceConfig;
	private PBCacheService cache;
	private PBTargetService target;
	private List<PBSourceService> sources;

	public PBLoadedServices(InstanceConfig instanceConfig, PBCacheService cache, PBTargetService target, List<PBSourceService> sources) {
		super();
		this.instanceConfig = instanceConfig;
		this.cache = cache;
		this.target = target;
		this.sources = sources;
	}

	public PBCacheService getCache() {
		return cache;
	}

	public void setCache(PBCacheService cache) {
		this.cache = cache;
	}

	public PBTargetService getTarget() {
		return target;
	}

	public void setTarget(PBTargetService target) {
		this.target = target;
	}

	public List<PBSourceService> getSources() {
		return sources;
	}

	public void setSources(List<PBSourceService> sources) {
		this.sources = sources;
	}

	public InstanceConfig getInstanceConfig() {
		return instanceConfig;
	}

	public void setInstanceConfig(InstanceConfig instanceConfig) {
		this.instanceConfig = instanceConfig;
	}

}
