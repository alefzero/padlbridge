package com.alefzero.padlbridge.orchestrator.model;

import java.util.Objects;

public class CacheEntry {

	private String dn;
	private String uid;
	private String hash;

	public CacheEntry(String dn) {
		super();
		Objects.requireNonNull(dn);
		this.dn = dn;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
