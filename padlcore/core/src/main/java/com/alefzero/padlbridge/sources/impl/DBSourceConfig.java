package com.alefzero.padlbridge.sources.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

import com.alefzero.padlbridge.config.model.OperationalActions;
import com.alefzero.padlbridge.config.model.SourceConfig;
import com.alefzero.padlbridge.util.PInfo;

public class DBSourceConfig extends SourceConfig {

	private String jdbcURL;
	private String username = "";
	private String password = "";
	private String query;
	private String uid;
	private Deque<String> datamap;
	private Deque<String> multiValueAttributes = new ArrayDeque<String>();
	private Deque<String> objectClasses;
	private String dialect = "com.alefzero.padlbridge.sources.dialects.MariaDBDialectHelper";

	private Map<String, String> dbColToLDAP = null;
	private Map<String, String> ldapColToDB = null;
	private Deque<String> objectClassesAsAttributes = new ArrayDeque<String>();

	@Override
	public void checkConfiguration() {
		super.checkConfiguration();
		Objects.requireNonNull(getDn(),
				PInfo.msg("config.required-attribute-not-found", "dn", "source", this.getName()));
		Objects.requireNonNull(jdbcURL,
				PInfo.msg("config.required-attribute-not-found", "jdbcURL", "source", this.getName()));
		Objects.requireNonNull(query,
				PInfo.msg("config.required-attribute-not-found", "query", "source", this.getName()));
		Objects.requireNonNull(uid, PInfo.msg("config.required-attribute-not-found", "uid", "source", this.getName()));

		if (OperationalActions.UPDATE != getDefaultOperation()) {
			Objects.requireNonNull(objectClasses,
					PInfo.msg("config.required-attribute-not-found", "objectClasses", "source", this.getName()));
		}

	}

	public Map<String, String> getLdapToDBMap() {
		return ldapColToDB;
	}

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
		this.uid = Objects.requireNonNull(uid);
	}

	public Deque<String> getDatamap() {
		return datamap;
	}

	public void setDatamap(Deque<String> datamap) {

		dbColToLDAP = new HashMap<String, String>();
		ldapColToDB = new HashMap<String, String>();

		for (String item : datamap) {
			StringTokenizer stEqual = new StringTokenizer(item, "=");
			String ldapCol = stEqual.nextToken().trim();
			String dbCol = stEqual.countTokens() == 0 ? ldapCol : stEqual.nextToken().trim();
			dbColToLDAP.put(dbCol, ldapCol);
			ldapColToDB.put(ldapCol, dbCol);
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

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialectHelperClass) {
		this.dialect = dialectHelperClass;
	}

	@Override
	public String toString() {
		return "DBSourceConfig [dbColToLDAP=" + dbColToLDAP + ", ldapColToDB=" + ldapColToDB
				+ ", objectClassesAsAttributes=" + objectClassesAsAttributes + ", jdbcURL=" + jdbcURL + ", username="
				+ username + ", password=" + password + ", query=" + query + ", uid=" + uid + ", datamap=" + datamap
				+ ", multiValueAttributes=" + multiValueAttributes + ", objectClasses=" + objectClasses + ", dialect="
				+ dialect + ", getType()=" + getType() + ", getName()=" + getName() + ", getDn()=" + getDn()
				+ ", getDefaultOperation()=" + getDefaultOperation() + "]";
	}

	public String getLdapAttributeNameFor(String dbColumn) {
		return Objects.requireNonNull(dbColToLDAP.get(dbColumn),
				"Database column " + dbColumn + " not found in dataMap. Check your configuration");
	}

	public String getDBAttributeNameFor(String ldapColumn) {
		return Objects.requireNonNull(ldapColToDB.get(ldapColumn),
				"Ldap column " + ldapColumn + " not found in dataMap. Check your configuration");
	}

	public Deque<String> getAllObjectClasses() {
		return objectClassesAsAttributes;
	}

}
