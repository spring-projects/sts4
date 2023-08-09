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
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.boot.modulith.AppModules;
import org.springframework.ide.vscode.boot.modulith.ModulithService;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;

public class ModulithTypeReferenceViolationReconciler implements JdtAstReconciler, ApplicationContextAware {
	
	private ApplicationContext appContext;

	@Override
	public void reconcile(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector,
			boolean isCompleteAst) throws RequiredCompleteAstException {
		Path sourceFile = Paths.get(docUri);
		if (IClasspathUtil.getProjectJavaSourceFoldersWithoutTests(project.getClasspath())
				.anyMatch(f -> sourceFile.startsWith(f.toPath()))) {
			ModulithService modulithService = appContext.getBean(ModulithService.class); 
			AppModules appModules = modulithService.getModulesData(project);

			if (appModules != null) {
				final String packageName = cu.getPackage().getName().getFullyQualifiedName();
				cu.accept(new ASTVisitor() {
					@Override
					public boolean visit(ImportDeclaration node) {
						if (!node.isOnDemand()) {
							String typeFqName = node.getName().getFullyQualifiedName();
							appModules.getModuleNotExposingType(packageName, getBinaryName(typeFqName)).ifPresent(module -> {
								problemCollector.accept(new ReconcileProblemImpl(getProblemType(),
										"Cannot use type in this package. Type is not exposed in module '"
												+ module.name() + "'.",
										node.getName().getStartPosition(), node.getName().getLength()));
							});
						}
						return false;
					}

					@Override
					public boolean visit(SimpleType node) {
						if (node.getName().isQualifiedName()) {
							appModules.getModuleNotExposingType(packageName, getBinaryName(node.getName().getFullyQualifiedName())).ifPresent(module -> {
								problemCollector.accept(new ReconcileProblemImpl(getProblemType(),
										"Cannot use type in this package. Type is not exposed in module '"
												+ module.name() + "'.",
										node.getName().getStartPosition(), node.getName().getLength()));
							});
						} else if (node.getName().isSimpleName()) {
							String typeName = node.getName().getFullyQualifiedName();
							for (Object i : cu.imports()) {
								ImportDeclaration importDecl = (ImportDeclaration) i;
								if (importDecl.isOnDemand()) {
									appModules.getModuleNotExposingType(packageName, getBinaryName(importDecl.getName().getFullyQualifiedName() + "." + typeName)).ifPresent(module -> {
										problemCollector.accept(new ReconcileProblemImpl(getProblemType(),
												"Cannot use type in this package. Type is not exposed in module '"
														+ module.name() + "'.",
												node.getName().getStartPosition(), node.getName().getLength()));
									});
								}
							}
						}
						return super.visit(node);
					}
				});
				
			}
		}
	}
	
	private static String getBinaryName(String fqName) {
		String pkgName = ModulithService.getPackageNameFromTypeFQName(fqName);
		if (pkgName.length() < fqName.length() - 1) {
			String typeName = fqName.substring(pkgName.length() + 1);
			StringBuilder sb = new StringBuilder();
			sb.append(pkgName);
			sb.append('.');
			sb.append(typeName.replace('.', '$'));
			return sb.toString();
		}
		return fqName;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return ModulithService.isModulithDependentProject(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot3JavaProblemType.MODULITH_TYPE_REF_VIOLATION;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.appContext = applicationContext;
	}

}
