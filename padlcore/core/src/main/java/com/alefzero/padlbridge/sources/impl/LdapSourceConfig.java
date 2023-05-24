package com.alefzero.padlbridge.sources.impl;

import java.util.Objects;

import com.alefzero.padlbridge.config.model.SourceConfig;

public class LdapSourceConfig extends SourceConfig {
	private String host;
	private int port = 389;
	private String username;
	private String password;
	private String useTLS;
	private String baseDN;

	@Override
	public void checkConfiguration() {
		super.checkConfiguration();
		Objects.requireNonNull(host, "Required attribute objectClasses not found in source configuration.");
		Objects.requireNonNull(baseDN, "Required attribute baseDN not found in source configuration.");
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUseTLS() {
		return useTLS;
	}

	public void setUseTLS(String useTLS) {
		this.useTLS = useTLS;
	}

	public String getBaseDN() {
		return baseDN;
	}

	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}

	@Override
	public String toString() {
		return "LdapSourceConfig [host=" + host + ", port=" + port + ", username=" + username + ", password=" + password
				+ ", useTLS=" + useTLS + ", baseDN=" + baseDN + "]";
	}

}
