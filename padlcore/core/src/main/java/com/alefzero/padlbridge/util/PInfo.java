package com.alefzero.padlbridge.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PInfo {
	protected static final Logger logger = LogManager.getLogger();

	private static final String BUNDLE_NAME = "locale/messages/padl";
	private static final String LOG_BUNDLE_NAME = "locale/log/padl_logging";

	private static ResourceBundle msgBundle = ResourceBundle.getBundle(BUNDLE_NAME);
	private static ResourceBundle logBundle = ResourceBundle.getBundle(LOG_BUNDLE_NAME);

	private PInfo() {
		super();
	}

	public static void setLocale(Locale locale) {
		msgBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
		logBundle = ResourceBundle.getBundle(LOG_BUNDLE_NAME, locale);
	}

	public static String msg(String key) {
		return msgBundle.getString(key);
	}

	public static String msg(String key, Object... items) {
		return String.format(msgBundle.getString(key), items);
	}

	public static String msg(String key, String[] items) {
		return String.format(msgBundle.getString(key),  Arrays.toString(items));
	}

	public static String log(String key) {
		return logBundle.getString(key);
	}

	public static String log(String key, Object... items) {
		return String.format(logBundle.getString(key), items);
	}

	public static String log(String key, String[] items) {
		return String.format(logBundle.getString(key), Arrays.toString(items));
	}
}
