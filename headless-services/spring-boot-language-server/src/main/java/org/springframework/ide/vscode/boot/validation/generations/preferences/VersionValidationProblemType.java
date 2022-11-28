/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations.preferences;

import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.HINT;
import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.ERROR;

import org.springframework.ide.vscode.boot.common.SpringProblemCategories;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemCategory;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public enum VersionValidationProblemType implements ProblemType {

//	SUPPORTED_OSS_VERSION(HINT, "Supported OSS Boot Version", "Supported OSS Boot Version"),
//
//	UNSUPPORTED_OSS_VERSION(ERROR, "Unsupported OSS Version", "Unsupported OSS Version"),
//	
//	UNSUPPORTED_COMMERCIAL_VERSION(ERROR, "Unsupported Commercial Version", "Unsupported Commercial Version"),
//	
//	SUPPORTED_COMMERCIAL_VERSION(HINT, "Supported Commercial Version", "Supported Commercial Version"),
		
	UPDATE_LATEST_MAJOR_VERSION(HINT, "Update to Latest Major Version", "Update to Latest Major Version"),
	
	UPDATE_LATEST_MINOR_VERSION(HINT, "Update to Latest Minor Version", "Update to Latest Minor Version"),

	UPDATE_LATEST_PATCH_VERSION(HINT, "Update to Latest Patch Version", "Update to Latest Patch Version");

	private final ProblemSeverity defaultSeverity;
	private String description;
	private String label;

	private VersionValidationProblemType(ProblemSeverity defaultSeverity, String description, String label) {
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
		return SpringProblemCategories.VERSION_VALIDATION;
	}

}
