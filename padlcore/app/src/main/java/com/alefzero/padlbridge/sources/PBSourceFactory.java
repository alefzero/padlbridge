package com.alefzero.padlbridge.sources;

import com.alefzero.padlbridge.config.model.SourceConfig;
import com.alefzero.padlbridge.orchestrator.PBGenericFactory;

public interface PBSourceFactory extends PBGenericFactory {
	
	<T extends SourceConfig> Class<T> getConfigClass();

}
