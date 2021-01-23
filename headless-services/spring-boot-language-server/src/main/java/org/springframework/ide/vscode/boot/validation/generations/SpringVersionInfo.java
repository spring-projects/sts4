/*******************************************************************************
 * Copyright (c) 2021 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.io.File;

import org.springframework.ide.vscode.commons.java.SpringProjectUtil;

/**
 * Version info for a spring dependency.
 * 
 * For example, given a spring dependency name: "spring-boot-2.4.0-M4" , the slug is
 * "spring-boot", the fullVersion "2.4.0-M4", and the majMin "2.4"
 *
 */
public class SpringVersionInfo {

	private final String slug;
	private final String majMin;
	private final String fullVersion;

	/**
	 * 
	 * @param file spring for dependency, e.g. spring-boot-2.4.0-M4.jar
	 */
	public SpringVersionInfo(File file) {
		String fileName = file.getName();
		this.slug = SpringProjectUtil.getProjectSlug(fileName);
		this.majMin = SpringProjectUtil.getMajMinVersion(fileName);
		this.fullVersion = SpringProjectUtil.getVersion(fileName);
	}

	public String getSlug() {
		return slug;
	}

	public String getMajMin() {
		return majMin;
	}

	public String getFullVersion() {
		return fullVersion;
	}

}