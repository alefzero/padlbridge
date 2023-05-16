package com.alefzero.padlbridge.core.model;

import com.unboundid.ldap.sdk.Entry;

public class DataEntry {

	private Entry entry;
	private String hash;
	private String uid;

	public DataEntry(String uid, Entry entry, String hash) {
		super();
		this.uid = uid;
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

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
