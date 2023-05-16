package com.alefzero.padlbridge.sources.impl;

import java.util.Iterator;

import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.sources.PBSourceService;

public class LdapSourceService extends PBSourceService {

	@Override
	public String toString() {
		return "LdapService [toString()=" + super.toString() + "]";
	}

	@Override
	public Iterator<DataEntry> getAllEntries() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<String> getAllUids() {
		// TODO Auto-generated method stub
		return null;
	}

}
