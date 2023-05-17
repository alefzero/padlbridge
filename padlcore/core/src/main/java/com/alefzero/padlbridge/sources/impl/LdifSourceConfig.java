package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.config.model.SourceConfig;

public class LdifSourceConfig extends SourceConfig {

	private String ldif;

	public String getLdif() {
		return ldif;
	}

	public void setLdif(String ldif) {
		this.ldif = ldif;
	}

	@Override
	public String toString() {
		return "LdifSourceConfig [ldif=" + ldif + ", getName()=" + getName() + ", getDn()=" + getDn() + "]";
	}

}
