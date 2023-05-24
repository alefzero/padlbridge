package com.alefzero.padlbridge.config.model;

import java.util.Objects;

import com.alefzero.padlbridge.util.PInfo;

public abstract class SourceConfig implements PBGenericConfig {

	private String type;
	private String name;
	private String dn;
	private String defaultOperation;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getDefaultOperation() {
		return defaultOperation;
	}

	public void setDefaultOperation(String defaultOperation) {
		this.defaultOperation = defaultOperation;
	}

	@Override
	public void checkConfiguration() {
		Objects.requireNonNull(type);
		Objects.requireNonNull(name, PInfo.msg("config.required-attribute-not-found", "name", "source", this.getName()));
		Objects.requireNonNull(dn, PInfo.msg("config.required-attribute-not-found", "dn", "source", this.getName()));
	}

	@Override
	public String toString() {
		return "SourceConfig [type=" + type + ", name=" + name + ", dn=" + dn + ", defaultOperation=" + defaultOperation
				+ "]";
	}

}
