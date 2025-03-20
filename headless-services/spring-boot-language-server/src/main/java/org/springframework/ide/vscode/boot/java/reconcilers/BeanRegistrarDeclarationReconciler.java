/*******************************************************************************
 * Copyright (c) 2025 Broadcom, Inc.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot4JavaProblemType;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.Version;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.ImportBeanRegistrarInConfigRecipe;

public class BeanRegistrarDeclarationReconciler implements JdtAstReconciler {

	private final QuickfixRegistry registry;
	private final SpringMetamodelIndex springIndex;

	public BeanRegistrarDeclarationReconciler(QuickfixRegistry registry, SpringMetamodelIndex springIndex) {
		this.registry = registry;
		this.springIndex = springIndex;
	}
	
	@Override
	public boolean isApplicable(IJavaProject project) {
		Version version = SpringProjectUtil.getDependencyVersion(project, "spring-context");
		return version != null && version.compareTo(new Version(7, 0, 0, null)) >= 0;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot4JavaProblemType.REGISTRAR_BEAN_DECLARATION;
	}

	@Override
	public ASTVisitor createVisitor(IJavaProject project, URI docURI, CompilationUnit cu,
			IProblemCollector problemCollector, boolean isCompleteAst, boolean isIndexComplete) {
		return new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration node) {
				ITypeBinding type = node.resolveBinding();
				
				if (type == null) {
					return true;
				}
				
				if (ASTUtils.findInTypeHierarchy(type, Set.of(Annotations.BEAN_REGISTRAR_INTERFACE)) == null) {
					return true;
				}
				
				if (!isIndexComplete) {
					throw new RequiredCompleteIndexException();
				}
					
				List<Bean> configBeans = new ArrayList<>();
				Path p = Path.of(docURI);
				List<Path> sourceFolders = IClasspathUtil.getSourceFolders(project.getClasspath()).map(f -> f.toPath()).filter(f -> p.startsWith(f)).collect(Collectors.toList());

				for (Bean b : springIndex.getBeansOfProject(project.getElementName())) {
					//						if (b.getType().equals(type.getQualifiedName())) {
					//							return true;
					//						}
					if (b.isConfiguration() && b.getLocation() != null) {
						Path configBeanPath = Path.of(URI.create(b.getLocation().getUri()));
						if (sourceFolders.stream().anyMatch(configBeanPath::startsWith)) {
							configBeans.add(b);
						}
					}
				}

				if (configBeans.isEmpty() || !isImportedBeanRegistrarInConfig(configBeans, type)) {
					ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(), "Bean not registered", node.getName().getStartPosition(), node.getName().getLength());
					List<FixDescriptor> fixes = configBeans.stream()
							.filter(b -> b.getLocation() != null && b.getLocation().getUri() != null)
							.map(b -> new FixDescriptor(ImportBeanRegistrarInConfigRecipe.class.getName(), List.of(b.getLocation().getUri()), "Add %s to `@Import` in %s".formatted(type.getName(), b.getName()))
									.withParameters(Map.of(
											"configBeanFqn", b.getType(),
											"beanRegFqn", type.getQualifiedName()
											))
									.withRecipeScope(RecipeScope.FILE)
									).toList();
					ReconcileUtils.setRewriteFixes(registry, problem, fixes);
					problemCollector.accept(problem);
				}
				return true;
			}
			
		};
	}
	
	private boolean isImportedBeanRegistrarInConfig(List<Bean> configBeans, ITypeBinding beanRegType) {
		return configBeans.stream()
				.flatMap(bean -> Arrays.stream(bean.getAnnotations())) // look into annotations on this bean definition
				.filter(annotation -> Annotations.IMPORT.equals(annotation.getAnnotationType())) // look into @Import annotations only
				.flatMap(annotation -> annotation.getAttributes().get("value") == null ? Stream.empty() : Arrays.stream(annotation.getAttributes().get("value"))) // look into the attribute values of "value" attribute
				.anyMatch(annotationValue -> annotationValue.getName().equals(beanRegType.getQualifiedName()));
	}

}
