package com.alefzero.padlbridge.sources.impl;

import java.util.ArrayDeque;
import java.util.Iterator;

import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.unboundid.ldap.sdk.Entry;

public class ConfigSourceService extends PBSourceService {
	
	@Override
	public String toString() {
		return "Source1Service [toString()=" + super.toString() + "]";
	}

	@Override
	public Iterator<DataEntry> getAllEntries() {
		return new ArrayDeque<DataEntry>().iterator();
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

}
