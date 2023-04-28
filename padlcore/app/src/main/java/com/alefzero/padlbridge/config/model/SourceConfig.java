package com.alefzero.padlbridge.config.model;

public class SourceConfig implements PBGenericConfig {

	private String type;
	private String name;
	private String dn;

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

	@Override
	public String toString() {
		return "SourceConfig [type=" + type + ", name=" + name + ", dn=" + dn + "]";
	}

}
