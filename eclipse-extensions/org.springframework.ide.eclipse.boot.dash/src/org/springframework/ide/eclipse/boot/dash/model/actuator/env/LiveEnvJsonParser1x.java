/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.actuator.env;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ide.eclipse.beans.ui.live.model.JsonParser;

import com.google.common.collect.ImmutableList;

public class LiveEnvJsonParser1x implements JsonParser<LiveEnvModel> {

	public LiveEnvJsonParser1x() {
	}

	@Override
	public LiveEnvModel parse(String jsonInput) throws Exception {
		JSONObject json = toJson(jsonInput);
		ActiveProfiles profiles = parseProfiles(json);
		PropertySources propertySources = parseProperties(json);
		return new LiveEnvModel(profiles, propertySources);
	}

	protected JSONObject toJson(String json) throws JSONException {
		return new JSONObject(json);
	}

	private ActiveProfiles parseProfiles(JSONObject envObj) {
		Object _profiles = envObj.opt("profiles");
		ImmutableList.Builder<Profile> list = ImmutableList.builder();

		if (_profiles instanceof JSONArray) {
			JSONArray profilesObj = (JSONArray) _profiles;

			for (int i = 0; i < profilesObj.length(); i++) {
				Object object = profilesObj.opt(i);
				if (object instanceof String) {
					list.add(new Profile((String) object));
				}
			}
		}
		List<Profile> profiles = list.build();
		return new Profiles1x(profiles);
	}

	/**
	 *
	 * @param envObj
	 * @return non-null PropertySources. Content in the PropertySources may be empty
	 *         if no sources are found
	 * @throws Exception
	 */
	private PropertySources parseProperties(JSONObject allSourcesJson) throws Exception {
		ImmutableList.Builder<PropertySource> allSources = ImmutableList.builder();

		if (allSourcesJson != null) {
			Iterator<?> keys = allSourcesJson.keys();
			if (keys != null) {
				while (keys.hasNext()) {
					Object key = keys.next();
					if (key instanceof String) {
						String sourceName = (String) key;
						// Skip profiles. It is parsed separately
						if (!"profiles".equals(sourceName)) {
							Object sourceObj = allSourcesJson.opt(sourceName);
							PropertySource propertySource = new PropertySource(sourceName);
							allSources.add(propertySource);
							if (sourceObj instanceof JSONObject) {
								JSONObject source = (JSONObject) sourceObj;
								Iterator<?> propKeys = source.keys();
								if (propKeys != null) {
									ImmutableList.Builder<Property> parsedProps = ImmutableList.builder();
									while (propKeys.hasNext()) {
										Object propObjKey = propKeys.next();
										if (propObjKey instanceof String) {
											String propName = (String) propObjKey;
											Object valObj = source.optString(propName);
											if (valObj instanceof String) {
												String value = (String) valObj;
												Property property = new Property(propName, value, null);
												parsedProps.add(property);
											}
										}
									}
									propertySource.add(parsedProps.build());
								}
							}
						}
					}
				}
			}
		}

		List<PropertySource> sources = allSources.build();
		return new PropertySources(sources);
	}
}
