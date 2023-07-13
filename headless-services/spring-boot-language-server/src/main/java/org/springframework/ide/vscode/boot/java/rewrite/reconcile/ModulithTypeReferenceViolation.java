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

import java.util.Optional;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.marker.JavaSourceSet;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.java.tree.J.FieldAccess;
import org.openrewrite.java.tree.J.Identifier;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.boot.modulith.AppModules;
import org.springframework.ide.vscode.boot.modulith.ModulithService;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.MarkerVisitorContext;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.ProjectParser;

public class ModulithTypeReferenceViolation implements RecipeCodeActionDescriptor {
	
	private static final String MSG_PKG_NAME = "packageName";

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(MarkerVisitorContext context) {
		ModulithService modulithService = context.appContext().getBean(ModulithService.class);
		AppModules appModules = modulithService.getModulesData(context.project());
		
		return new JavaIsoVisitor<ExecutionContext>() {

			@Override
			public CompilationUnit visitCompilationUnit(CompilationUnit cu, ExecutionContext p) {
				if (appModules == null) {
					return cu;
				} else {
					JavaSourceSet sourceSet = cu.getMarkers().findFirst(JavaSourceSet.class).orElse(null);
					if (sourceSet != null && ProjectParser.TEST.equals(sourceSet.getName())) {
						return cu;
					}
					String pkgName = cu.getPackageDeclaration() == null ? "" : cu.getPackageDeclaration().getPackageName();
					p.putMessage(MSG_PKG_NAME, pkgName);
					return super.visitCompilationUnit(cu, p);
				}
			}
			
			

			@Override
			public FieldAccess visitFieldAccess(FieldAccess fieldAccess, ExecutionContext p) {
				FieldAccess fa = super.visitFieldAccess(fieldAccess, p);
				return process(fa, p.getMessage(MSG_PKG_NAME), TypeUtils.asFullyQualified(fa.getType()));
			}

			@Override
			public Identifier visitIdentifier(Identifier identifier, ExecutionContext p) {
				Identifier i = super.visitIdentifier(identifier, p);
				if (getCursor().getParent().firstEnclosingOrThrow(J.class) instanceof J.FieldAccess) {
					return i;
				}
				// check if identifier is a simple name of the type
				FullyQualified type = TypeUtils.asFullyQualified(identifier.getType());
				if (type != null && identifier.getSimpleName().equals(type.getClassName())) {
					return process(i, p.getMessage(MSG_PKG_NAME), type);
				}
				
				return i;
			}

			private <T extends J> T process(T node, String packageName, FullyQualified type) {
				if (type != null) {
					Optional<T> opt = appModules.getModuleNotExposingType(packageName, type.getFullyQualifiedName()).map(module -> {
						FixAssistMarker fixMarker = new FixAssistMarker(Tree.randomId(), getId())
								.withLabel("Cannot use type in this package. Type is not exposed in module '" + module.name() + "'." );
						return node.withMarkers(node.getMarkers().add(fixMarker));
					});
					return opt.orElse(node);
				}
				return node;
			}

		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return ModulithService.isModulithDependentProject(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot3JavaProblemType.MODULITH_TYPE_REF_VIOLATION;
	}

}
