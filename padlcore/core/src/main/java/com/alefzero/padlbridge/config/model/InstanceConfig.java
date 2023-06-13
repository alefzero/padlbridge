package com.alefzero.padlbridge.config.model;

public class InstanceConfig {

	private String lang;
	private String version = "no-version";
	private String instanceId;

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@Override
	public String toString() {
		return "GeneralConfig [lang=" + lang + ", version=" + version + ", instanceId=" + instanceId + "]";
	}

}
