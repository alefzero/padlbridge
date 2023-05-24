package com.alefzero.padlbridge.sources.impl;

import java.util.Objects;

import com.alefzero.padlbridge.config.model.SourceConfig;
import com.alefzero.padlbridge.util.PInfo;

public class LdifSourceConfig extends SourceConfig {

	private String ldif;

	@Override
	public void checkConfiguration() {
		super.checkConfiguration();
		Objects.requireNonNull(getDn(), PInfo.msg("config.required-attribute-not-found", "dn", "source", this.getName()));
		Objects.requireNonNull(ldif, PInfo.msg("config.required-attribute-not-found", "ldif", "source", this.getName()));
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
