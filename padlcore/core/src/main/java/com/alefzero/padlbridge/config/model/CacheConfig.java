package com.alefzero.padlbridge.config.model;

public abstract class CacheConfig implements PBGenericConfig {

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "CacheConfig [type=" + type + "]";
	}

}
