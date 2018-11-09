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
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.collect.ImmutableList;

public class ContextPath {

	protected static Logger logger = LoggerFactory.getLogger(ContextPath.class);

	public static final Collection<String> BOOT_1X_CONTEXTPATH = ImmutableList.of("server.context-path",
			"server.contextPath");

	public static final Collection<String> BOOT_2X_CONTEXTPATH = ImmutableList.of("server.servlet.context-path",
			"server.servlet.contextPath");

	public static String getContextPath(String bootVersion, String environment) {

		if (environment != null) {
			JSONObject env = new JSONObject(environment);

			Collection<String> contextPathProperties = null;
			if ("1.x".equals(bootVersion)) {
				contextPathProperties = BOOT_1X_CONTEXTPATH;
			} else if ("2.x".equals(bootVersion)) {
				contextPathProperties = BOOT_2X_CONTEXTPATH;
			}

			if (contextPathProperties != null) {
				for (String prop : contextPathProperties) {
					String contextPath = findContextPath(env, prop);
					if (StringUtil.hasText(contextPath)) {
						return contextPath;
					}
				}
			}
		}

		return null;
	}

	private static String findContextPath(JSONObject env, String contextPathProp) {
		String contextPath = null;
		if (env != null) {
			// Properties defined in command line args have higher priority over
			// those defined in application configuration files (properties/yaml files)
			contextPath = findInCommandLineArgs(env, contextPathProp);
			if (contextPath == null) {
				contextPath = findInApplicationConfig(env, contextPathProp);
			}
		}
		return contextPath;
	}

	private static String findInApplicationConfig(JSONObject env, String contextPathProp) {
		// boot 1.x
		JSONObject applicationConfig = null;
		for (String key : env.keySet()) {
			if (key.startsWith("applicationConfig")) {
				applicationConfig = env.getJSONObject(key);
				if (applicationConfig != null) {
					String contextPathValue = applicationConfig.optString(contextPathProp);
					// Warning: fetching value above may return empty string, so null check on the
					// value is not enough
					if (StringUtil.hasText(contextPathValue)) {
						return contextPathValue;
					}
				}
			}
		}

		// boot 2.x
		if (applicationConfig == null) {
			// Not found as direct property value... in Boot 2.0 we must look inside the
			// 'propertySources'.
			// Similar... but structure is more complex.
			JSONArray propertySources = env.optJSONArray("propertySources");
			if (propertySources != null) {
				for (Object _source : propertySources) {
					if (_source instanceof JSONObject) {
						JSONObject source = (JSONObject) _source;
						String sourceName = source.optString("name");
						if (sourceName != null && sourceName.startsWith("applicationConfig")) {
							JSONObject props = source.optJSONObject("properties");
							Set<String> keySet = props.keySet();
							// Check that the context is a key before retrieving the JSON object value.
							// Note: attempting to fetch the JSON object value on a key that may not exist
							// throws exception
							// thus the reason why we are checking that the key exists first
							if (keySet.contains(contextPathProp)) {
								JSONObject jsonObject = props.getJSONObject(contextPathProp);
								if (jsonObject != null) {
									String contextPathValue = jsonObject.optString("value");
									if (StringUtil.hasText(contextPathValue)) {
										return contextPathValue;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	protected static String findInCommandLineArgs(JSONObject env, String contextPathProp) {
		// boot 1.x
		JSONObject commandLineArgs = env.optJSONObject("commandLineArgs");
		if (commandLineArgs != null) {
			String contextPathValue = commandLineArgs.optString(contextPathProp);
			// Warning: fetching value above may return empty string, so null check on the
			// value is not enough
			if (StringUtil.hasText(contextPathValue)) {
				return contextPathValue;
			}
		}
		// boot 2.x
		if (commandLineArgs == null) {
			// Not found as direct property value... in Boot 2.0 we must look inside the
			// 'propertySources'.
			// Similar... but structure is more complex.
			JSONArray propertySources = env.optJSONArray("propertySources");
			if (propertySources != null) {
				for (Object _source : propertySources) {
					if (_source instanceof JSONObject) {
						JSONObject source = (JSONObject) _source;
						String sourceName = source.optString("name");
						if ("commandLineArgs".equals(sourceName)) {
							JSONObject props = source.optJSONObject("properties");
							// Find the contextPathProp in the command line args
							JSONObject valueObject = props.optJSONObject(contextPathProp);
							if (valueObject != null) {
								String contextPathValue = valueObject.optString("value");
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
