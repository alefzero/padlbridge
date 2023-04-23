package com.alefzero.padlbridge.targets.impl;

import com.alefzero.padlbridge.config.model.TargetConfig;

public class Target1Config extends TargetConfig {

	private String host;
	private int port = 10389;
	private String useTLS;
	private String rootDN;

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

	public String getUseTLS() {
		return useTLS;
	}

	public void setUseTLS(String useTLS) {
		this.useTLS = useTLS;
	}

	public String getRootDN() {
		return rootDN;
	}

	public void setRootDN(String rootDN) {
		this.rootDN = rootDN;
	}

	@Override
	public String toString() {
		return "Target1Config [type=" + super.getType() + ", host=" + host + ", port=" + port + ", useTLS=" + useTLS
				+ ", rootDN=" + rootDN + "]";
	}

}
