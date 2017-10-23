/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.conditionals;

public class Constants {

	public static final String CONDITIONAL_ON_BEAN = "org.springframework.boot.autoconfigure.condition.ConditionalOnBean";
	public static final String CONDITIONAL_ON_MISSING_BEAN = "org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean";
	public static final String CONDITIONAL_ON_PROPERTY = "org.springframework.boot.autoconfigure.condition.ConditionalOnProperty";
	public static final String CONDITIONAL_ON_RESOURCE = "org.springframework.boot.autoconfigure.condition.ConditionalOnResource";

	public static final String CONDITIONAL_ON_CLASS = "org.springframework.boot.autoconfigure.condition.ConditionalOnClass";
	public static final String CONDITIONAL_ON_CLOUD_PLATFORM = "org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform";
	public static final String CONDITIONAL_ON_WEB_APPLICATION = "org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication";
}
