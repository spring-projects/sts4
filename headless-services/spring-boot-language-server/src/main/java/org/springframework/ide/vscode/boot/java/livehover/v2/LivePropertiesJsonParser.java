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

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class LivePropertiesJsonParser {

	/**
	 * Parse live properties from the given JSON input. Returns null if no live
	 * properties could be parsed
	 *
	 * @param jsonInput
	 * @return live properties if parsed, or null otherwise
	 * @throws Exception
	 */
	public LiveProperties parse(String jsonInput) throws Exception {

		JSONObject envObj = toJson(jsonInput);

		List<LivePropertySource> propertySources = readProperties(envObj);
		if (propertySources != null && !propertySources.isEmpty()) {
			return new LiveProperties(propertySources);
		} else {
			return null;
		}
	}

	protected JSONObject toJson(String json) throws JSONException {
		return new JSONObject(json);
	}

	protected abstract List<LivePropertySource> readProperties(JSONObject envObj) throws Exception;

	public static LiveProperties parseProperties(String jsonInput) throws Exception {
		LivePropertiesJsonParser2x boot2x = new LivePropertiesJsonParser2x();
		LiveProperties properties = boot2x.parse(jsonInput);
		if (properties == null) {
			LivePropertiesJsonParser1x boot1x = new LivePropertiesJsonParser1x();
			properties = boot1x.parse(jsonInput);
		}
		return properties;
	}

}
