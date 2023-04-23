package com.alefzero.padlbridge.targets;

import com.alefzero.padlbridge.config.model.TargetConfig;
import com.alefzero.padlbridge.orchestrator.PBGenericFactory;

public interface PBTargetFactory extends PBGenericFactory {

	<T extends TargetConfig> Class<T> getConfigClass();

}
