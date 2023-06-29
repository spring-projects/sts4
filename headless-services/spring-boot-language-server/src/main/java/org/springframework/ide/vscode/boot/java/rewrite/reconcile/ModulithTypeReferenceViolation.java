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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
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
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.MarkerVisitorContext;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class ModulithTypeReferenceViolation implements RecipeCodeActionDescriptor {
	
	private static final String MSG_PKG_NAME = "packageName";

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(MarkerVisitorContext context) {
		ModulithService modulithService = context.appContext().getBean(ModulithService.class);
		CompletableFuture<AppModules> future = modulithService.getModulesData(context.project());
		
		AppModules modules = null;
		try {
			modules = future == null ? null : future.get();
		} catch (InterruptedException | ExecutionException e) {
			// ignore
		}
		
		final AppModules appModules = modules;
		
		return new JavaIsoVisitor<ExecutionContext>() {

			@Override
			public CompilationUnit visitCompilationUnit(CompilationUnit cu, ExecutionContext p) {
				if (appModules == null) {
					return cu;
				} else {
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
				if (!(getCursor().getParent().firstEnclosingOrThrow(J.class) instanceof J.FieldAccess)) {
					return process(i, p.getMessage(MSG_PKG_NAME), TypeUtils.asFullyQualified(identifier.getType()));
				}
				return i;
			}

			private <T extends J> T process(T node, String packageName, FullyQualified type) {
				if (type != null) {
					if (!appModules.isReferenceAllowed(packageName, type.getFullyQualifiedName())) {
						FixAssistMarker fixMarker = new FixAssistMarker(Tree.randomId(), getId())
								.withLabel("Type is not allowed to be used in this package. Consider changing you 'Modulith' structure." );
						node = node.withMarkers(node.getMarkers().add(fixMarker));
					}
				}
				return node;
			}

		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		Version v = SpringProjectUtil.getDependencyVersion(project, "spring-modulith-core");
		return v != null;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot3JavaProblemType.MODULITH_TYPE_REF_VIOLATION;
	}

}
