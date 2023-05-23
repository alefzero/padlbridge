package com.alefzero.padlbridge.cache.impl;

import com.alefzero.padlbridge.config.model.CacheConfig;

/**
 * Represents YAML configuration for the MariaDB based cache
 * 
 * @author xandecelo
 *
 */
public class MariaDBCacheConfig extends CacheConfig {

	private String jdbcURL;
	private String username;
	private String password;

	public String getJdbcURL() {
		return jdbcURL;
	}

	public void setJdbcURL(String jdbcURL) {
		this.jdbcURL = jdbcURL;
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

}
