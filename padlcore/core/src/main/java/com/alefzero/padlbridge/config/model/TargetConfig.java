package com.alefzero.padlbridge.config.model;

public class TargetConfig  implements PBGenericConfig {

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "TargetConfig [type=" + type + "]";
	}

}
