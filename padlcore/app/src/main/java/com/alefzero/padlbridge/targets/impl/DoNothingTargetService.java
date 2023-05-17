package com.alefzero.padlbridge.targets.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alefzero.padlbridge.targets.PBTargetService;
import com.unboundid.ldap.sdk.Entry;

public class DoNothingTargetService extends PBTargetService {
	protected static final Logger logger = LogManager.getLogger();

	@Override
	public void prepare() {
		// do nothing
	}

	@Override
	public void add(Entry entry) {
		// do nothing
	}

	@Override
	public void modify(Entry entry) {
		// do nothing
	}

	@Override
	public void delete(Entry entry) {
		// do nothing
	}

	@Override
	public void addAll(Iterator<Entry> entriesToAddFrom) {
		// do nothing
	}

	@Override
	public Deque<String> deleteAll(Iterator<String> listOfDNsToDelete) {
		Deque<String> _return = new ArrayDeque<String>();
		// do nothing
		return _return;
	}


}
