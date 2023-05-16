package com.alefzero.padlbridge.core.model;

import com.unboundid.ldap.sdk.Entry;

public class DataEntry {

	private Entry entry;
	private String hash;

	public DataEntry(Entry entry, String hash) {
		super();
		this.entry = entry;
		this.hash = hash;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
