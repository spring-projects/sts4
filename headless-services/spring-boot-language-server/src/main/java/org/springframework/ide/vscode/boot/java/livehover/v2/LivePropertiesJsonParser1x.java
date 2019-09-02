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

import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.google.common.collect.ImmutableList;

public class LivePropertiesJsonParser1x extends LivePropertiesJsonParser {

	public LivePropertiesJsonParser1x() {

	}

	/**
	 *
	 * @param envObj
	 * @return non-null PropertySources. Content in the PropertySources may be empty
	 *         if no sources are found
	 * @throws Exception
	 */
	@Override
	protected List<LivePropertySource> readProperties(JSONObject allSourcesJson) throws Exception {
		ImmutableList.Builder<LivePropertySource> allSources = ImmutableList.builder();

		if (allSourcesJson != null) {
			Iterator<?> keys = allSourcesJson.keys();
			if (keys != null) {
				while (keys.hasNext()) {
					Object key = keys.next();
					if (key instanceof String) {
						String sourceName = (String) key;
						// Skip profiles
						if (!"profiles".equals(sourceName)) {
							Object sourceObj = allSourcesJson.opt(sourceName);
							ImmutableList.Builder<LiveProperty> parsedProps = ImmutableList.builder();

							if (sourceObj instanceof JSONObject) {
								JSONObject source = (JSONObject) sourceObj;
								Iterator<?> propKeys = source.keys();
								if (propKeys != null) {
									while (propKeys.hasNext()) {
										Object propObjKey = propKeys.next();
										if (propObjKey instanceof String) {
											String propName = (String) propObjKey;
											Object valObj = source.optString(propName);
											if (valObj instanceof String) {
												String value = (String) valObj;
												LiveProperty property = LiveProperty
														.builder() //
														.source(sourceName) //
														.property(propName) //
														.value(value) //
														.build();
												parsedProps.add(property);
											}
										}
									}
								}
							}
							LivePropertySource propertySource = new LivePropertySource(sourceName, parsedProps.build());
							allSources.add(propertySource);
						}
					}
				}
			}
		}

		return allSources.build();
	}
}
