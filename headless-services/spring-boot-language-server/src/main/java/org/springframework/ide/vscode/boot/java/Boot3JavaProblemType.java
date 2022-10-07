/*******************************************************************************
 * Copyright (c) 2020, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.ERROR;
import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.WARNING;

import org.springframework.ide.vscode.boot.common.SpringProblemCategories;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

/**
 * This enum is supposed to represent *all* the different types of problems SpringBoot language server
 * may detect in Java code.
 */
public enum Boot3JavaProblemType implements ProblemType {
	
	JAVA_CONCRETE_BEAN_TYPE(WARNING, "Bean definition should have precise type for Spring 6 AOT", "Not precise bean defintion type"),
	
	JAVA_BEAN_POST_PROCESSOR_IGNORED_IN_AOT(WARNING, "'BeanPostProcessor' behaviour is ignored in Spring 6 AOT", "'BeanPostProcessor' behaviour is ignored in AOT"),
	
	JAVA_BEAN_NOT_REGISTERED_IN_AOT(WARNING, "Not registered as Bean", "Not registered as a Bean"),
	
	JAVA_TYPE_NOT_SUPPORTED(ERROR, "Type no supported as of Spring Boot 3", "Type not supported as of Spring Boot 3");
	
	private final ProblemSeverity defaultSeverity;
	private String description;
	private String label;

	private Boot3JavaProblemType(ProblemSeverity defaultSeverity, String description) {
		this(defaultSeverity, description, null);
	}

	private Boot3JavaProblemType(ProblemSeverity defaultSeverity, String description, String label) {
		this.description = description;
		this.defaultSeverity = defaultSeverity;
		this.label = label;
	}

	@Override
	public ProblemSeverity getDefaultSeverity() {
		return defaultSeverity;
	}

	public String getLabel() {
		if (label==null) {
			label = createDefaultLabel();
		}
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	private String createDefaultLabel() {
		String label = this.toString().substring(5).toLowerCase().replace('_', ' ');
		return Character.toUpperCase(label.charAt(0)) + label.substring(1);
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public ProblemCategory getCategory() {
		return SpringProblemCategories.BOOT_3;
	}
	
}
