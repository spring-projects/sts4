/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

/**
 * Constants containing various fully-qualified annotation names.
 *
 * @author Kris De Volder
 */
public class Annotations {
	public static final String BEAN = "org.springframework.context.annotation.Bean";
	public static final String PROFILE = "org.springframework.context.annotation.Profile";
	public static final String CONDITIONAL = "org.springframework.context.annotation.Conditional";

	public static final String COMPONENT = "org.springframework.stereotype.Component";
	public static final String REPOSITORY = "org.springframework.stereotype.Repository";

	public static final String AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";
	public static final String INJECT = "javax.inject.Inject";

	public static final String QUALIFIER = "org.springframework.beans.factory.annotation.Qualifier";

	public static final String SPRING_REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
	public static final String SPRING_GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
	public static final String SPRING_POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
	public static final String SPRING_PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
	public static final String SPRING_DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
	public static final String SPRING_PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";

	public static final String CONDITIONAL_ON_BEAN = "org.springframework.boot.autoconfigure.condition.ConditionalOnBean";
	public static final String CONDITIONAL_ON_MISSING_BEAN = "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean";
	public static final String CONDITIONAL_ON_PROPERTY = "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty";
	public static final String CONDITIONAL_ON_RESOURCE = "org.springframework.boot.autoconfigure.condition.ConditionalOnResource";
	public static final String CONDITIONAL_ON_CLASS = "org.springframework.boot.autoconfigure.condition.ConditionalOnClass";
	public static final String CONDITIONAL_ON_MISSING_CLASS = "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass";
	public static final String CONDITIONAL_ON_CLOUD_PLATFORM = "org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform";
	public static final String CONDITIONAL_ON_WEB_APPLICATION = "org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication";
	public static final String CONDITIONAL_ON_NOT_WEB_APPLICATION = "org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication";
	public static final String CONDITIONAL_ON_ENABLED_INFO_CONTRIBUTOR = "org.springframework.boot.actuate.autoconfigure.ConditionalOnEnabledInfoContributor";
	public static final String CONDITIONAL_ON_ENABLED_RESOURCE_CHAIN = "org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain";
	public static final String CONDITIONAL_ON_ENABLED_ENDPOINT = "org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint";
	public static final String CONDITIONAL_ON_ENABLED_HEALTH_INDICATOR = "org.springframework.boot.actuate.autoconfigure.ConditionalOnEnabledHealthIndicator";
	public static final String CONDITIONAL_ON_EXPRESSION = "org.springframework.boot.autoconfigure.condition.ConditionalOnExpression";
	public static final String CONDITIONAL_ON_JAVA = "org.springframework.boot.autoconfigure.condition.ConditionalOnJava";
	public static final String CONDITIONAL_ON_JNDI = "org.springframework.boot.autoconfigure.condition.ConditionalOnJndi";
	public static final String CONDITIONAL_ON_SINGLE_CANDIDATE = "org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate";

}
