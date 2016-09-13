package com.github.kdvolder.lsapi.util;

import java.util.Map;

/**
 * Convenience wrapper around a 'settings' object. Provides some useful accessor methods to
 * retrieve properties from the settings object.
 */
public class Settings {
	
	private Object settings;
	
	public Settings(Object settings) {
		this.settings = settings;
	}

	public Integer getInt(String... names) {
		Object val = getProperty(names);
		if (val instanceof Number) {
			return ((Number) val).intValue();
		}
		return null;
	}
	
	public Object getProperty(String... names) {
		return getProperty(settings, names, 0);
	}

	@SuppressWarnings("rawtypes")
	private static Object getProperty(Object settings, String[] names, int i) {
		if (i >= names.length) {
			return settings;
		} else if (settings instanceof Map) {
			Object sub = ((Map)settings).get(names[i]);
			return getProperty(sub, names, i+1);
		} else {
			return null;
		}
	}



}
