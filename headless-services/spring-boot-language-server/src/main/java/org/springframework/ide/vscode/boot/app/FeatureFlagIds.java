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

/**
 * The complete list of valid ids for the {@code boot-java.feature-flags} setting. The ids are part of
 * the settings contract and must stay stable once released. Each id is paired with a {@link FeatureFlag}
 * constant; when adding an id, add it here, to {@link FeatureFlag} and to the description of the setting
 * in the {@code package.json} of the VS Code extension.
 *
 * @author Martin Lippert
 */
public final class FeatureFlagIds {

	private FeatureFlagIds() {
	}

	/** Property key proposals in {@code @Value} */
	public static final String VALUE_PROPERTY_COMPLETION = "value-property-completion";

	/** {@code classpath:} resource proposals in {@code @Value} */
	public static final String VALUE_CLASSPATH_COMPLETION = "value-classpath-completion";

	/** Go to definition / references on property keys in {@code @Value} and {@code @ConditionalOnProperty} */
	public static final String PROPERTY_KEY_NAVIGATION = "property-key-navigation";

	/** Go to definition / references on bean names in {@code @DependsOn}, {@code @Qualifier}, {@code @Resource}, {@code @Named}, {@code @ConditionalOn(Missing)Bean} */
	public static final String BEAN_NAME_NAVIGATION = "bean-name-navigation";

	/** References on profile names in {@code @Profile} */
	public static final String PROFILE_NAVIGATION = "profile-navigation";

	/** Bean name proposals in {@code @DependsOn}, {@code @Qualifier}, {@code @Resource}, {@code @Named}, {@code @ConditionalOn(Missing)Bean} */
	public static final String BEAN_NAME_COMPLETION = "bean-name-completion";

	/** Bean type proposals in {@code @ConditionalOn(Missing)Bean} */
	public static final String BEAN_TYPE_COMPLETION = "bean-type-completion";

	/** Profile proposals in {@code @Profile} */
	public static final String PROFILE_COMPLETION = "profile-completion";

	/** Key proposals in {@code @ConditionalOnProperty} */
	public static final String CONDITIONAL_ON_PROPERTY_COMPLETION = "conditional-on-property-completion";

	/** Scope proposals in {@code @Scope} */
	public static final String SCOPE_COMPLETION = "scope-completion";

	/** Quick fixes for problems whose severity is set to {@code IGNORE}, offered as refactorings */
	public static final String IGNORED_PROBLEM_FIXES = "ignored-problem-fixes";

	/** The "Convert query to text block" code action */
	public static final String QUERY_TEXT_BLOCK_CONVERSION = "query-text-block-conversion";

	/** Resource proposals in {@code @ContextConfiguration} */
	public static final String CONTEXT_CONFIGURATION_COMPLETION = "context-configuration-completion";

	/** Query method name proposals in Spring Data repository interfaces */
	public static final String DATA_REPOSITORY_COMPLETION = "data-repository-completion";

	/** Resource proposals in {@code @ConditionalOnResource} */
	public static final String CONDITIONAL_ON_RESOURCE_COMPLETION = "conditional-on-resource-completion";

	/** Cron expression proposals in {@code @Scheduled} */
	public static final String CRON_COMPLETION = "cron-completion";

	/** Java snippet proposals, e.g. request mapping methods in controllers */
	public static final String JAVA_SNIPPET_COMPLETION = "java-snippet-completion";

	/** Go to definition on {@code classpath:} resources in annotation values */
	public static final String CLASSPATH_RESOURCE_NAVIGATION = "classpath-resource-navigation";

	/** Go to definition on parameters in data queries */
	public static final String DATA_QUERY_PARAMETER_NAVIGATION = "data-query-parameter-navigation";

	/** Go to definition in SpEL expressions */
	public static final String SPEL_NAVIGATION = "spel-navigation";

	/** Go to the generated implementation of Spring Data query methods */
	public static final String AOT_QUERY_METHOD_IMPLEMENTATION_NAVIGATION = "aot-query-method-implementation-navigation";

	/** References on application events */
	public static final String EVENT_NAVIGATION = "event-navigation";

	/** The "Add @Query" code action for query methods */
	public static final String ADD_QUERY_ANNOTATION = "add-query-annotation";

	/** The quick fix to set the SQL dialect for native queries */
	public static final String SQL_DIALECT_QUICKFIX = "sql-dialect-quickfix";

	/** Code lens on WebFlux handler methods */
	public static final String WEBFLUX_HANDLER_CODE_LENS = "webflux-handler-code-lens";

	/** Code lenses that send queries, SpEL and pointcut expressions to an AI assistant */
	public static final String AI_ASSISTANT_CODE_LENS = "ai-assistant-code-lens";

	/** Code lens on functional router methods to convert them to the builder pattern */
	public static final String ROUTER_FUNCTION_CODE_LENS = "router-function-code-lens";

	/** Code lens with the AOT generated query on Spring Data query methods */
	public static final String DATA_QUERY_AOT_CODE_LENS = "data-query-aot-code-lens";

	/** Code lens with web config details on controller classes */
	public static final String WEB_CONFIG_CODE_LENS = "web-config-code-lens";

	/** Hovers on property keys in {@code application.properties} files */
	public static final String PROPERTIES_HOVER = "properties-hover";

	/** Hovers on property keys in {@code application.yml} / {@code application.yaml} files */
	public static final String YAML_HOVER = "yaml-hover";

	/** Document highlights for WebFlux routes in Java files */
	public static final String WEBFLUX_ROUTE_HIGHLIGHT = "webflux-route-highlight";

	/** Document highlights for parts of data queries (JPQL, HQL, SQL) in Java files */
	public static final String DATA_QUERY_HIGHLIGHT = "data-query-highlight";

	/** Inlay hints in data queries (JPQL, HQL, SQL) in Java files */
	public static final String DATA_QUERY_INLAY_HINTS = "data-query-inlay-hints";

	/** Spring Boot version and starter inlay hints in {@code pom.xml} files */
	public static final String POM_INLAY_HINTS = "pom-inlay-hints";

	/** Syntax highlighting of data queries (JPQL, HQL, SQL) in Java files and in named query properties files */
	public static final String DATA_QUERY_SEMANTIC_TOKENS = "data-query-semantic-tokens";

	/** Syntax highlighting of SpEL expressions in Java files */
	public static final String SPEL_SEMANTIC_TOKENS = "spel-semantic-tokens";

	/** Syntax highlighting of cron expressions in Java files and in properties and YAML files */
	public static final String CRON_SEMANTIC_TOKENS = "cron-semantic-tokens";

	/** Property key and value proposals in {@code application.properties} files */
	public static final String PROPERTIES_COMPLETION = "properties-completion";

	/** Property key and value proposals in {@code application.yml} / {@code application.yaml} files */
	public static final String YAML_COMPLETION = "yaml-completion";

	/** Go to definition on property keys and values in {@code application.properties} files */
	public static final String PROPERTIES_NAVIGATION = "properties-navigation";

	/** Go to definition on property keys and values in {@code application.yml} / {@code application.yaml} files */
	public static final String YAML_NAVIGATION = "yaml-navigation";

	/** The document symbols (outline) of Java, Spring XML and {@code spring.factories} files. The underlying index stays active */
	public static final String DOCUMENT_SYMBOLS = "document-symbols";

	/** The workspace symbol search (not the live application URL queries starting with //). The underlying index stays active */
	public static final String WORKSPACE_SYMBOLS = "workspace-symbols";

}
