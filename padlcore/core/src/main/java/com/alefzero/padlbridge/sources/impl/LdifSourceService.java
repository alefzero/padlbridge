package com.alefzero.padlbridge.sources.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;

import com.alefzero.padlbridge.core.model.DataEntry;
import com.alefzero.padlbridge.exceptions.PadlUnrecoverableError;
import com.alefzero.padlbridge.sources.PBSourceService;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;

public class LdifSourceService extends PBSourceService {

	private LdifSourceConfig config;

	@Override
	public Iterator<DataEntry> getAllEntries() {
		Deque<DataEntry> _return = new ArrayDeque<DataEntry>();
		Entry entry;
		try {
			entry = new Entry(config.getLdif().split("\n"));
			_return.add(new DataEntry(config.getDn(), entry, DigestUtils.md5Hex(config.getDn())));
		} catch (LDIFException e) {
			e.printStackTrace();
			throw new PadlUnrecoverableError(String.format("LDIF for source %s cannot be parsed. Found: %s",
					config.getName(), config.getLdif().split("\n")));
		}
		return _return.iterator();
	}

	@Override
	public void prepare() {
		config = (LdifSourceConfig) super.getConfig();
	}

	@Override
	public Iterator<String> getAllUids() {
		Deque<String> _return = new ArrayDeque<String>();
		_return.add(config.getDn());
		return _return.iterator();
	}

}
