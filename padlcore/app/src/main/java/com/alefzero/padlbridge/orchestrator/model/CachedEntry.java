package com.alefzero.padlbridge.orchestrator.model;

import java.util.Objects;

public class CachedEntry implements Comparable<CachedEntry> {

	private String dn;
	private String hash;

	public CachedEntry(String dn) {
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

	@Override
	public int compareTo(CachedEntry o) {
		Objects.requireNonNull(o);
		return dn.compareToIgnoreCase(o.dn);
	}


}
