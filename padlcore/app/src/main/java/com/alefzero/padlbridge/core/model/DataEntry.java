package com.alefzero.padlbridge.core.model;

import com.unboundid.ldap.sdk.Entry;

public class DataEntry {

	public static final int OPERATION_CHECK = 0;
	public static final int OPERATION_ADD = 1;
	public static final int OPERATION_MODIFY = 2;

	private Entry entry;
	private int operation;
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

	public int getOperation() {
		return operation;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
