package com.alefzero.padlbridge.targets.impl;

import com.alefzero.padlbridge.config.model.TargetConfig;

public class GenericLdapTargetConfig extends TargetConfig {

	private String host;
	private int port = 389;
	private String useTLS;
	private String rootDN;
	private String adminPassword;
	private String adminUser;

	@Override
	public void checkConfiguration() {
		super.checkConfiguration();
		assert host != null : "Required attribute host not found in target configuration.";
		assert rootDN != null : "Required attribute rootDN not found in target configuration.";
	}

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

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public String getAdminUser() {
		return adminUser;
	}

	public void setAdminUser(String adminUser) {
		this.adminUser = adminUser;
	}

	@Override
	public String toString() {
		return "GenericLdapConfig [host=" + host + ", port=" + port + ", useTLS=" + useTLS + ", rootDN=" + rootDN
				+ ", adminPassword=" + adminPassword + ", adminUser=" + adminUser + ", getType()=" + getType() + "]";
	}

}
