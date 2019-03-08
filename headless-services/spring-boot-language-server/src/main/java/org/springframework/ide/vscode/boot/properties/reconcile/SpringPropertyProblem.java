/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.properties.reconcile;

import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;

// TODO: Move to a common project shared between YAML and Properties

public class SpringPropertyProblem extends ReconcileProblemImpl {

	private PropertyInfo property = null;
	private String propertyName;

	public SpringPropertyProblem(ProblemType type, String msg, int offset, int len) {
		super(type, msg, offset, len);
	}

	public static SpringPropertyProblem problem(ApplicationPropertiesProblemType type, String msg, int offset, int len) {
		return new SpringPropertyProblem(type, msg, offset, len);
	}

	public static SpringPropertyProblem problem(ApplicationPropertiesProblemType type, String msg, DocumentRegion region) {
		if (region.isEmpty()) {
			region = makeVisible(region);
		}
		return new SpringPropertyProblem(type, msg, region.getStart(), region.getLength());
	}

	public void setMetadata(PropertyInfo property) {
		this.property = property;
	}

	public void setPropertyName(String name) {
		propertyName = name;
	}

}
