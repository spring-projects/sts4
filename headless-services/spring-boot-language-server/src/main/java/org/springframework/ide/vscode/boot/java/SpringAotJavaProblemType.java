/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.WARNING;

import java.util.List;

import org.eclipse.lsp4j.DiagnosticTag;
import org.springframework.ide.vscode.boot.common.SpringProblemCategories;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public enum SpringAotJavaProblemType implements ProblemType {
	
	JAVA_CONCRETE_BEAN_TYPE(WARNING, "Bean definition should have precise type", "Not precise bean defintion type"),
	JAVA_BEAN_POST_PROCESSOR_IGNORED_IN_AOT(WARNING, "'BeanPostProcessor' behaviour is ignored", "'BeanPostProcessor' behaviour is ignored in AOT"),
	JAVA_BEAN_NOT_REGISTERED_IN_AOT(WARNING, "Not registered as Bean", "Not registered as a Bean");

	private final ProblemSeverity defaultSeverity;
	private final String description;
	private String label;
	private final List<DiagnosticTag> tags;

	private SpringAotJavaProblemType(ProblemSeverity defaultSeverity, String description, String label, List<DiagnosticTag> tags) {
		this.description = description;
		this.defaultSeverity = defaultSeverity;
		this.label = label;
		this.tags = tags;
	}

	private SpringAotJavaProblemType(ProblemSeverity defaultSeverity, String description, String label) {
		this(defaultSeverity, description, label, null);
	}

	private SpringAotJavaProblemType(ProblemSeverity defaultSeverity, String description) {
		this(defaultSeverity, description, null);
	}

	@Override
	public ProblemSeverity getDefaultSeverity() {
		return defaultSeverity;
	}

	public String getLabel() {
		if (label == null) {
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
		return SpringProblemCategories.SPRING_AOT;
	}

	@Override
	public List<DiagnosticTag> getTags() {
		return tags;
	}

}
