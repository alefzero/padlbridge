package com.alefzero.padlbridge.sources.impl;

import java.util.ArrayDeque;
import java.util.Iterator;

import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.sources.PBSourceService;

public class LdifSourceService extends PBSourceService {
	
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

	@Override
	public Iterator<String> getAllUids() {
		// TODO Auto-generated method stub
		return null;
	}

}
