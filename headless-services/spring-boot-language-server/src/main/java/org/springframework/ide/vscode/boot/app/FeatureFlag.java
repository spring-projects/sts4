/*******************************************************************************
 * Copyright (c) 2026 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.util.Arrays;
import java.util.Optional;

/**
 * Individual language server features that can be turned on or off via the
 * {@code boot-java.feature-flags} setting. The ids are defined (and documented) in {@link FeatureFlagIds}.
 *
 * @author Martin Lippert
 */
public enum FeatureFlag {

	VALUE_PROPERTY_COMPLETION(FeatureFlagIds.VALUE_PROPERTY_COMPLETION),
	VALUE_CLASSPATH_COMPLETION(FeatureFlagIds.VALUE_CLASSPATH_COMPLETION),
	PROPERTY_KEY_NAVIGATION(FeatureFlagIds.PROPERTY_KEY_NAVIGATION),
	BEAN_NAME_NAVIGATION(FeatureFlagIds.BEAN_NAME_NAVIGATION),
	PROFILE_NAVIGATION(FeatureFlagIds.PROFILE_NAVIGATION),
	BEAN_NAME_COMPLETION(FeatureFlagIds.BEAN_NAME_COMPLETION),
	BEAN_TYPE_COMPLETION(FeatureFlagIds.BEAN_TYPE_COMPLETION),
	PROFILE_COMPLETION(FeatureFlagIds.PROFILE_COMPLETION),
	CONDITIONAL_ON_PROPERTY_COMPLETION(FeatureFlagIds.CONDITIONAL_ON_PROPERTY_COMPLETION),
	SCOPE_COMPLETION(FeatureFlagIds.SCOPE_COMPLETION),
	IGNORED_PROBLEM_FIXES(FeatureFlagIds.IGNORED_PROBLEM_FIXES),
	QUERY_TEXT_BLOCK_CONVERSION(FeatureFlagIds.QUERY_TEXT_BLOCK_CONVERSION),
	CONTEXT_CONFIGURATION_COMPLETION(FeatureFlagIds.CONTEXT_CONFIGURATION_COMPLETION),
	DATA_REPOSITORY_COMPLETION(FeatureFlagIds.DATA_REPOSITORY_COMPLETION),
	CONDITIONAL_ON_RESOURCE_COMPLETION(FeatureFlagIds.CONDITIONAL_ON_RESOURCE_COMPLETION),
	CRON_COMPLETION(FeatureFlagIds.CRON_COMPLETION),
	JAVA_SNIPPET_COMPLETION(FeatureFlagIds.JAVA_SNIPPET_COMPLETION),
	CLASSPATH_RESOURCE_NAVIGATION(FeatureFlagIds.CLASSPATH_RESOURCE_NAVIGATION),
	DATA_QUERY_PARAMETER_NAVIGATION(FeatureFlagIds.DATA_QUERY_PARAMETER_NAVIGATION),
	SPEL_NAVIGATION(FeatureFlagIds.SPEL_NAVIGATION),
	AOT_QUERY_METHOD_IMPLEMENTATION_NAVIGATION(FeatureFlagIds.AOT_QUERY_METHOD_IMPLEMENTATION_NAVIGATION),
	EVENT_NAVIGATION(FeatureFlagIds.EVENT_NAVIGATION),
	ADD_QUERY_ANNOTATION(FeatureFlagIds.ADD_QUERY_ANNOTATION),
	SQL_DIALECT_QUICKFIX(FeatureFlagIds.SQL_DIALECT_QUICKFIX),
	WEBFLUX_HANDLER_CODE_LENS(FeatureFlagIds.WEBFLUX_HANDLER_CODE_LENS),
	AI_ASSISTANT_CODE_LENS(FeatureFlagIds.AI_ASSISTANT_CODE_LENS),
	ROUTER_FUNCTION_CODE_LENS(FeatureFlagIds.ROUTER_FUNCTION_CODE_LENS),
	DATA_QUERY_AOT_CODE_LENS(FeatureFlagIds.DATA_QUERY_AOT_CODE_LENS),
	WEB_CONFIG_CODE_LENS(FeatureFlagIds.WEB_CONFIG_CODE_LENS),
	PROPERTIES_HOVER(FeatureFlagIds.PROPERTIES_HOVER),
	YAML_HOVER(FeatureFlagIds.YAML_HOVER),
	WEBFLUX_ROUTE_HIGHLIGHT(FeatureFlagIds.WEBFLUX_ROUTE_HIGHLIGHT),
	DATA_QUERY_HIGHLIGHT(FeatureFlagIds.DATA_QUERY_HIGHLIGHT),
	DATA_QUERY_INLAY_HINTS(FeatureFlagIds.DATA_QUERY_INLAY_HINTS),
	POM_INLAY_HINTS(FeatureFlagIds.POM_INLAY_HINTS),
	DATA_QUERY_SEMANTIC_TOKENS(FeatureFlagIds.DATA_QUERY_SEMANTIC_TOKENS),
	SPEL_SEMANTIC_TOKENS(FeatureFlagIds.SPEL_SEMANTIC_TOKENS),
	CRON_SEMANTIC_TOKENS(FeatureFlagIds.CRON_SEMANTIC_TOKENS),
	PROPERTIES_COMPLETION(FeatureFlagIds.PROPERTIES_COMPLETION),
	YAML_COMPLETION(FeatureFlagIds.YAML_COMPLETION),
	PROPERTIES_NAVIGATION(FeatureFlagIds.PROPERTIES_NAVIGATION),
	YAML_NAVIGATION(FeatureFlagIds.YAML_NAVIGATION),
	DOCUMENT_SYMBOLS(FeatureFlagIds.DOCUMENT_SYMBOLS),
	WORKSPACE_SYMBOLS(FeatureFlagIds.WORKSPACE_SYMBOLS);

	private final String id;

	FeatureFlag(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public static Optional<FeatureFlag> fromId(String id) {
		return Arrays.stream(values()).filter(f -> f.id.equals(id)).findFirst();
	}

}
