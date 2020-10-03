/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Convenience wrapper around a 'settings' object. Provides some useful accessor methods to
 * retrieve properties from the settings object.
 */
public class Settings {

	private static final Logger log = LoggerFactory.getLogger(Settings.class);

	private JsonElement settings;

	private Gson gson;

	public Settings(JsonElement settings) {
		this.settings = settings;
	}

	public <T> T getAs(Class<T> type, String... names) {
		JsonElement json = getRawProperty(names);
		if (json!=null) {
			return gson().fromJson(json, type);
		}
		return null;
	}

	private Gson gson() {
		if (gson==null) {
			gson = new Gson();
		}
		return gson;
	}

	public Set<String> getStringSet(String... names) {
		ImmutableSet.Builder<String> strings = ImmutableSet.builder();
		try {
			JsonElement val = getRawProperty(names);
			if (val != null) {
				JsonArray array = val.getAsJsonArray();
				for (JsonElement el : array) {
					try {
						strings.add(el.getAsString());
					} catch (Exception e) {
						log.error("", e);
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return strings.build();
	}

	public Integer getInt(String... names) {
		try {
			JsonElement val = getRawProperty(names);
			if (val != null) {
				return val.getAsInt();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	public String getString(String... names) {
		try {
			JsonElement val = getRawProperty(names);
			if (val != null) {
				return val.getAsString();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	public Boolean getBoolean(String... names) {
		try {
			JsonElement val = getRawProperty(names);
			if (val != null) {
				return val.getAsBoolean();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	public JsonElement getRawProperty(String... names) {
		return getRawProperty(settings, names, 0);
	}

	public JsonElement getRawSettings() {
		return settings;
	}

	private static JsonElement getRawProperty(JsonElement settings, String[] names, int i) {
		if (i >= names.length) {
			return settings;
		} else if (settings instanceof JsonObject) {
			JsonElement sub = ((JsonObject)settings).get(names[i]);
			return getRawProperty(sub, names, i+1);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return ""+settings;
	}

	public Settings navigate(String... names) {
		if (this.settings!=null && !this.settings.isJsonNull()) {
			if (names.length==0) {
				return this;
			} else if (this.settings.isJsonObject()) {
				JsonObject obj = (JsonObject) this.settings;
				if (obj.has(names[0])) {
					Settings sub = new Settings(obj.get(names[0]));
					return sub.navigate(cdr(names));
				}
			}
		}
		return new Settings(JsonNull.INSTANCE);
	}

	private String[] cdr(String[] names) {
		String[] rest = new String[names.length-1];
		for (int i = 0; i < rest.length; i++) {
			rest[i] = names[i+1];
		}
		return rest;
	}

	public Iterable<String> keys() {
		if (settings!=null && settings.isJsonObject()) {
			JsonObject obj = (JsonObject) settings;
			return obj.keySet();
		}
		return ImmutableList.of();
	}

}
