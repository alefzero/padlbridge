package com.alefzero.padlbridge.sources.impl;

import com.alefzero.padlbridge.config.model.SourceConfig;

public class LdapSourceConfig extends SourceConfig {
	private String host;
	private int port;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "LdapSourceConfig [host=" + host + ", port=" + port + ", getType()=" + getType() + ", getName()="
				+ getName() + "]";
	}

}
