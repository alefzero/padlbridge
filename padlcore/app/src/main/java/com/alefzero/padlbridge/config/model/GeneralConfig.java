package com.alefzero.padlbridge.config.model;

public class GeneralConfig {

	private String lang;
	private String version;

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

	@Override
	public String toString() {
		return "GeneralConfig [lang=" + lang + ", version=" + version + "]";
	}

}
