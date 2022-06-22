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
package org.springframework.ide.vscode.boot.java.rewrite.codeaction;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.util.Map;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J.Block;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.rewrite.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.boot.java.rewrite.RecipeScope;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.rewrite.java.AnnotationHierarchies;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class AutowiredFieldIntoConstructorParameterCodeAction implements RecipeCodeActionDescriptor {
	
	private static final String LABEL = "Convert @Autowired field into Constructor Parameter";
	private static final String ID = "org.springframework.ide.vscode.commons.rewrite.java.ConvertAutowiredFieldIntoConstructorParameter";
	private static final String AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";

	@Override
	public String getRecipeId() {
		return ID;
	}

	@Override
	public String getLabel(RecipeScope s) {
		return RecipeCodeActionDescriptor.buildLabel(LABEL, s);
	}

	@Override
	public RecipeScope[] getScopes() {
		return new RecipeScope[] { RecipeScope.NODE };
	}

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor() {
		return new JavaIsoVisitor<>() {

			@Override
			public VariableDeclarations visitVariableDeclarations(VariableDeclarations multiVariable,
					ExecutionContext p) {
				VariableDeclarations m = super.visitVariableDeclarations(multiVariable, p);
				Cursor blockCursor = getCursor().dropParentUntil(Block.class::isInstance);
				if (multiVariable.getVariables().size() == 1
						&& multiVariable.getLeadingAnnotations().stream().anyMatch(a -> TypeUtils.isOfClassType(a.getType(), AUTOWIRED))
						&& blockCursor.getParent().getValue() instanceof ClassDeclaration) {
					ClassDeclaration classDeclaration = (ClassDeclaration) blockCursor.getParent().getValue();
					FullyQualified fqType = TypeUtils.asFullyQualified(classDeclaration.getType());
					if (fqType != null && isApplicableType(fqType)) {
						m = m.withMarkers(m.getMarkers().add(new FixAssistMarker(Tree.randomId())
								.withRecipeId(getRecipeId())
								.withScope(classDeclaration.getId())
								.withParameters(Map.of("classFqName", fqType.getFullyQualifiedName(), "fieldName", multiVariable.getVariables().get(0).getSimpleName()))));
					}
				}
				return m;
			}

			private boolean isApplicableType(FullyQualified type) {
				return !AnnotationHierarchies
						.getTransitiveSuperAnnotations(type, fq -> fq.getFullyQualifiedName().startsWith("java."))
						.contains("org.springframework.boot.test.context.SpringBootTest");
			}
			
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 0, 0).test(project);
	}

}
