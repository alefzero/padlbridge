package com.alefzero.padlbridge.config.model;

public class TargetLDAPConfig {

	private String host;
	private Integer port;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "TargetLDAPConfig [host=" + host + ", port=" + port + "]";
	}

}
