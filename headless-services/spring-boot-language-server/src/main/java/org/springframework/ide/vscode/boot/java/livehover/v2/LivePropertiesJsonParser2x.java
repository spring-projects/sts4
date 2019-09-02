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
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ImmutableList;

public class LivePropertiesJsonParser2x extends LivePropertiesJsonParser {

	public LivePropertiesJsonParser2x() {

	}

	/**
	 *
	 * @param envObj
	 * @return non-null PropertySources. Content in the PropertySources may be empty
	 *         if no sources are found
	 * @throws Exception
	 */
	@Override
	protected List<LivePropertySource> readProperties(JSONObject envObj) throws Exception {
		ImmutableList.Builder<LivePropertySource> allSources = ImmutableList.builder();

		Object sourcesObj = envObj.opt("propertySources");

		if (sourcesObj instanceof JSONArray) {
			JSONArray props = (JSONArray) sourcesObj;
			for (int i = 0; i < props.length(); i++) {
				Object object = props.opt(i);
				if (object instanceof JSONObject) {
					JSONObject propObj = (JSONObject) object;
					String sourceName = propObj.optString("name");
					if (sourceName != null) {
						Object opt2 = propObj.opt("properties");
						List<LiveProperty> properties = parseProperties(sourceName, opt2);

						LivePropertySource propertySource = new LivePropertySource(sourceName, properties);

						allSources.add(propertySource);
					}
				}
			}
		}
		return allSources.build();
	}

	private List<LiveProperty> parseProperties(String sourceName, Object opt2) {
		List<LiveProperty> properties = new ArrayList<>();

		if (opt2 instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) opt2;
			Iterator<?> keys = jsonObj.keys();
			if (keys != null) {
				while (keys.hasNext()) {
					Object key = keys.next();
					if (key instanceof String) {
						String propKey = (String) key;
						Object propContentObj = jsonObj.opt(propKey);
						if (propContentObj != null) {
							String value = getValue(propContentObj);
							LiveProperty property = LiveProperty.builder() //
									.source(sourceName) //
									.property(propKey) //
									.value(value) //
									.build();
							properties.add(property);
						}
					}
				}
			}
		}
		return properties;
	}

	private String getValue(Object propContentObj) {
		if (propContentObj instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) propContentObj;
			return jsonObj.optString("value");
		}
		return null;
	}
}
