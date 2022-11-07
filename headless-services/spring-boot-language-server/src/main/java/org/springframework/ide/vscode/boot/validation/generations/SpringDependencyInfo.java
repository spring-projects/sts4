/*******************************************************************************
 * Copyright (c) 2021, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;

/**
 * Version info for a spring dependency.
 * 
 * For example, given a spring dependency name: "spring-boot-2.4.0-M4" , the slug is
 * "spring-boot", the fullVersion "2.4.0-M4", and the majMin "2.4"
 *
 */
public class SpringDependencyInfo {

	private final String slug;
	private final Version version;

	/**
	 * 
	 * @param file spring for dependency, e.g. spring-boot-2.4.0-M4.jar
	 */
	public SpringDependencyInfo(IJavaProject project, String slug) {
		this.slug = slug;
		this.version = SpringProjectUtil.getDependencyVersion(project, slug);
	}

	public String getSlug() {
		return slug;
	}

	public Version getVersion() {
		return version;
	}
}