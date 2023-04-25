package com.alefzero.padlbridge.cache.impl;

import java.util.Iterator;

import com.alefzero.padlbridge.cache.PBCacheService;
import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.sources.PBSourceService;

public class MariaDBCacheService extends PBCacheService {

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addHashesFrom(PBSourceService source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<String> getDeletedEntriesFrom(PBSourceService source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void consolidate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<DataEntry> getEntriesToAddFrom(PBSourceService source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<DataEntry> getEntriesToModifyFrom(PBSourceService source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateTables() {
		// TODO Auto-generated method stub
		
	}

}
