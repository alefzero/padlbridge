package com.alefzero.padlbridge.sources.impl;

import java.util.Objects;

import com.alefzero.padlbridge.config.model.SourceConfig;

public class LdifSourceConfig extends SourceConfig {

	private String ldif;

	@Override
	public void checkConfiguration() {
		super.checkConfiguration();
		Objects.requireNonNull(ldif, "Required attribute ldif not found in source configuration.");
	}

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
