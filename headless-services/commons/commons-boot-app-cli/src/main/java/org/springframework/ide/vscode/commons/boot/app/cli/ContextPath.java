/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.collect.ImmutableList;

public class ContextPath {

	protected static Logger logger = LoggerFactory.getLogger(ContextPath.class);

	public static final Collection<String> BOOT_1X_CONTEXTPATH = ImmutableList.of("server.context-path",
			"server.contextPath", "SERVER_CONTEXT_PATH");

	public static final Collection<String> BOOT_2X_CONTEXTPATH = ImmutableList.of("server.servlet.context-path",
			"server.servlet.contextPath", "SERVER_SERVLET_CONTEXT_PATH");

	public static String getContextPath(String bootVersion, String environment) {
		String contextPath = null;
		if (environment != null) {
			JSONObject env = new JSONObject(environment);

			if ("1.x".equals(bootVersion)) {
				contextPath = findContextPathInBoot1x(env);
			} else if ("2.x".equals(bootVersion)) {
				contextPath = findContextPathInBoot2x(env);
			}
		}

		return contextPath;
	}

	private static String findContextPathInBoot1x(JSONObject env) {
		// IMPORTANT: The order in which the env objects appear are assumed to be the
		// priority order defined
		// by boot rules in terms of which property source has higher precedence. Iterate
		// through ALL
		// sources in the order obtained from the env JSON
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
