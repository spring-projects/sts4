/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite.reconcile;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.spring.boot2.search.EntityIdForRepositoryVisitor;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.marker.Marker;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.MarkerVisitorContext;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class EntityIdForRepoProblem implements RecipeCodeActionDescriptor {
	
	private static final String ID = "org.openrewrite.java.spring.boot2.search.EntityIdForRepositoryVisitor";

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(MarkerVisitorContext context) {
		boolean considerIdField = context.project() != null && SpringProjectUtil.hasSpecificLibraryOnClasspath(context.project(), "spring-data-mongodb-", true);
		return new EntityIdForRepositoryVisitor<>(considerIdField) {
			@Override
			protected Marker createMarker(JavaType domainIdType) {
				return new FixAssistMarker(Tree.randomId(), ID).withLabel("Expected Domain ID type is '" + domainIdType + "'");
			}
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 0, 0).test(project);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.DOMAIN_ID_FOR_REPOSITORY;
	}

}
