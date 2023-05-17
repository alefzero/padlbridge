package com.alefzero.padlbridge.orchestrator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.config.model.InstanceConfig;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.alefzero.padlbridge.targets.PBTargetService;

public class PBLoadedServices {

	private InstanceConfig instanceConfig;
	private PBCacheService cache;
	private PBTargetService target;
	private List<PBSourceService> sources = new ArrayList<PBSourceService>();

	// Important: The usage of LinkedHashMap warrants source order necessary to
	// processing
	private Map<String, PBSourceService> sourcesMap = new LinkedHashMap<String, PBSourceService>();

	public PBLoadedServices(InstanceConfig instanceConfig, PBCacheService cache, PBTargetService target,
			List<PBSourceService> sources) {
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
		sourcesMap = new LinkedHashMap<String, PBSourceService>();
		sources.forEach(source -> sourcesMap.put(source.getName(), source));
	}

	public PBSourceService getSourceByName(String name) {
		return sourcesMap.get(name);
	}

	public InstanceConfig getInstanceConfig() {
		return instanceConfig;
	}

	public void setInstanceConfig(InstanceConfig instanceConfig) {
		this.instanceConfig = instanceConfig;
	}

}
