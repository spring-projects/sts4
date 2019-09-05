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

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.collect.ImmutableList;

public class LiveContextPathUtil {

	protected static Logger logger = LoggerFactory.getLogger(LiveContextPathUtil.class);

	public static final Collection<String> BOOT_1X_CONTEXTPATH = ImmutableList.of("server.context-path",
			"server.contextPath", "SERVER_CONTEXT_PATH");

	public static final Collection<String> BOOT_2X_CONTEXTPATH = ImmutableList.of("server.servlet.context-path",
			"server.servlet.contextPath", "SERVER_SERVLET_CONTEXT_PATH");

	public static String getContextPath(String bootVersion, String environment) {
		String contextPath = null;
		if (environment != null) {
			JSONObject env = new JSONObject(environment);
			// IMPORTANT: We want to check property sources (e.g. command line args, config
			// files, env vars, etc..)
			// for properties IN THE ORDER that they appear in the raw JSON, as the
			// assumption is that order is the correct
			// priority order of these property sources. We want to return the property from
			// the highest priority source.
			//
			// In boot 2.x, the property sources appear in order in an ORDERED JSONArray
			// "propertySource" under the top-level JSONObject for the environment
			// but for boot 1.x, the property sources are all top level key/values in an
			// UNORDERED JSONObject. Therefore for now we only support searching in "priority order"
			// for Boot 2.x
			if ("1.x".equals(bootVersion)) {
				contextPath = findContextPathInBoot1x(env);
			} else if ("2.x".equals(bootVersion)) {
				contextPath = findContextPathInBoot2x(env);
			}
		}

		return contextPath;
	}

	private static String findContextPathInBoot1x(JSONObject env) {
		// LIMITATION: In Boot 1.x, property sources appear top level in an UNORDERED
		// JSONObject (the key set obtained from the JSON Object may not match the order of properties as they appear in the raw JSON.
		// Therefore for Boot 1.x we don't currently support "ordering" of property sources
		// We don't know which one is the highest priority, so we just return the first encountered property
		for (String key : env.keySet()) {
			JSONObject jsonObj = env.optJSONObject(key);
			if (jsonObj != null) {
				for (String prop : BOOT_1X_CONTEXTPATH) {
					String contextPathValue = jsonObj.optString(prop);
					// Warning: fetching value above may return empty string, so null check on the
					// value is not enough
					if (StringUtil.hasText(contextPathValue)) {
						return contextPathValue;
					}
				}
			}
		}
		return null;
	}

	private static String findContextPathInBoot2x(JSONObject env) {
		JSONArray propertySources = env.optJSONArray("propertySources");
		if (propertySources != null) {

			// IMPORTANT: The order in which the env objects appear are assumed to be the
			// priority order defined
			// by boot rules in terms of which property source has higher precedence. Iterate
			// through ALL
			// sources in the order obtained from the env JSON
			for (Object _source : propertySources) {
				if (_source instanceof JSONObject) {
					JSONObject source = (JSONObject) _source;
					JSONObject props = source.optJSONObject("properties");
					if (props != null) {
						for (String property : BOOT_2X_CONTEXTPATH) {
							JSONObject propertyObj = props.optJSONObject(property);
							if (propertyObj != null) {
								String contextPathValue = propertyObj.optString("value");
								if (StringUtil.hasText(contextPathValue)) {
									return contextPathValue;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

}
