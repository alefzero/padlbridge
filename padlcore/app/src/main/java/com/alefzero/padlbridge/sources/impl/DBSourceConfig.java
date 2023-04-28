package com.alefzero.padlbridge.sources.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import com.alefzero.padlbridge.config.model.SourceConfig;

public class DBSourceConfig extends SourceConfig {

	private Map<String, String> dbColToLDAP = new HashMap<String, String>();
	private Deque<String> objectClassesAsAttributes = new ArrayDeque<String>();
	private String jdbcURL;
	private String username;
	private String password;
	private String query;
	private String uid;
	private Deque<String> datamap;
	private Deque<String> multiValueAttributes;
	private Deque<String> objectClasses;
	private String dialectHelperClass = "com.alefzero.padlbridge.sources.dialects.MariaDBDialectHelper";

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

	public void setPassword(String passoword) {
		this.password = passoword;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Deque<String> getDatamap() {
		return datamap;
	}

	public void setDatamap(Deque<String> datamap) {
		for (String item : datamap) {
			StringTokenizer stEqual = new StringTokenizer(item, "=");
			String dbCol = stEqual.nextToken().trim();
			String ldapCol = stEqual.countTokens() == 0 ? dbCol : stEqual.nextToken().trim();
			dbColToLDAP.put(dbCol, ldapCol);
		}
		this.datamap = datamap;
	}

	public Deque<String> getMultiValueAttributes() {
		return multiValueAttributes;
	}

	public void setMultiValueAttributes(Deque<String> multiValueAttributes) {
		this.multiValueAttributes = multiValueAttributes;
	}

	public Collection<String> getObjectClasses() {
		return objectClasses;
	}

	public void setObjectClasses(Deque<String> objectClasses) {
		for (String objectClass : objectClasses) {
			objectClassesAsAttributes.add("objectClass: " + objectClass);
		}
		this.objectClasses = objectClasses;
	}

	public String getDialectHelperClass() {
		return dialectHelperClass;
	}

	public void setDialectHelperClass(String dialectHelperClass) {
		this.dialectHelperClass = dialectHelperClass;
	}

	@Override
	public String toString() {
		return "DBSourceConfig [jdbcURL=" + jdbcURL + ", username=" + username + ", password=" + password + ", query="
				+ query + ", uid=" + uid + ", datamap=" + datamap + ", multiValueAttributes=" + multiValueAttributes
				+ ", objectClasses=" + objectClasses + ", dialectHelperClass=" + dialectHelperClass + "]";
	}

	public String getLdapAttributeNameFor(String dbColumn) {
		return Objects.requireNonNull(dbColToLDAP.get(dbColumn));
	}

	public Deque<String> getAllObjectClasses() {
		return objectClassesAsAttributes;
	}

}
