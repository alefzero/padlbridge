package com.alefzero.padlbridge.config.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class UserConfig {

	private GeneralConfig generalConfig;
	private TargetConfig targetConfig;
	private List<SourceConfig> sourceConfigList = new LinkedList<SourceConfig>();

	public UserConfig(GeneralConfig generalConfig, TargetConfig targetConfig, List<SourceConfig> sourceConfigList) {
		super();
		this.generalConfig = Objects.requireNonNull(generalConfig);
		this.targetConfig = Objects.requireNonNull(targetConfig);
		this.sourceConfigList = Objects.requireNonNull(sourceConfigList);
	}

	@Override
	public String toString() {
		return "UserConfig [generalConfig=" + generalConfig + ", targetConfig=" + targetConfig + ", sourceConfigList="
				+ sourceConfigList + "]";
	}

}
