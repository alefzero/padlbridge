package com.alefzero.padlbridge.config.model;

import java.util.Objects;

import com.alefzero.padlbridge.util.PInfo;

public abstract class SourceConfig implements PBGenericConfig {

	private String type;
	private String name;
	private String dn;
	private OperationalActions defaultOperation = OperationalActions.ADD;

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

	public OperationalActions getDefaultOperation() {
		return defaultOperation;
	}

	public void setDefaultOperation(OperationalActions defaultOperation) {
		this.defaultOperation = defaultOperation;
	}

	public void setDefaultOperation(String defaultOperation) {
		this.defaultOperation = OperationalActions.valueOf(Objects.requireNonNull(defaultOperation).toUpperCase());
	}

	@Override
	public void checkConfiguration() {
		Objects.requireNonNull(type);
		Objects.requireNonNull(name,
				PInfo.msg("config.required-attribute-not-found", "name", "source", this.getName()));
	}

	@Override
	public String toString() {
		return "SourceConfig [type=" + type + ", name=" + name + ", dn=" + dn + ", defaultOperation=" + defaultOperation
				+ "]";
	}

}
