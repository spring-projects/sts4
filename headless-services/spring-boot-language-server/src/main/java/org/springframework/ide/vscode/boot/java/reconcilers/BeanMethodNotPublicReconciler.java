/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.openrewrite.java.spring.BeanMethodsNotPublic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRecipeRepository;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix.QuickfixData;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class BeanMethodNotPublicReconciler implements AnnotationReconciler {
		
	private static final Logger log = LoggerFactory.getLogger(BeanMethodNotPublicReconciler.class);
	
    private static final String ID = BeanMethodsNotPublic.class.getName();
    private static final String LABEL = "Remove 'public' from @Bean method";

	private final RewriteRecipeRepository recipeRepo;
	private final QuickfixRegistry quickfixRegistry;
    
    public BeanMethodNotPublicReconciler(RewriteRecipeRepository recipeRepo, QuickfixRegistry quickfixRegistry) {
		this.recipeRepo = recipeRepo;
		this.quickfixRegistry = quickfixRegistry;
    }
	
	@Override
	public void visit(IJavaProject project, IDocument doc, Annotation node, ITypeBinding typeBinding,
			IProblemCollector problemCollector) {
		
		if (Annotations.BEAN.equals(typeBinding.getQualifiedName()) && node.getParent() instanceof MethodDeclaration) {
			MethodDeclaration m = (MethodDeclaration) node.getParent();
			Version version = SpringProjectUtil.getDependencyVersion(project, SpringProjectUtil.SPRING_BOOT);
			
			if (version.getMajor() >= 2) {
				IMethodBinding methodBinding = m.resolveBinding();
				if (isNotOverridingPublicMethod(methodBinding)) {
					
					ReconcileProblemImpl problem = ((List<?>)m.modifiers()).stream()
							.filter(Modifier.class::isInstance)
							.map(Modifier.class::cast)
							.filter(modifier -> modifier.isPublic())
							.findFirst()
							.map(modifier -> new ReconcileProblemImpl(
									Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD, "super special DIAGNOSTICS with public @Bean method",
									modifier.getStartPosition(), modifier.getLength()))
							.orElse(new ReconcileProblemImpl(
									Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD, "super special DIAGNOSTICS with public @Bean method",
									m.getName().getStartPosition(), m.getName().getLength()));

					addQuickFixes(doc.getUri(), problem);
					
					problemCollector.accept(problem);
				}
			}
		}
	}
	
	private static final boolean isOverriding(IMethodBinding binding) {
		try {
			Field f = binding.getClass().getDeclaredField("binding");
			f.setAccessible(true);
			org.eclipse.jdt.internal.compiler.lookup.MethodBinding  value = (org.eclipse.jdt.internal.compiler.lookup.MethodBinding) f.get(binding);
			return value.isOverriding();
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}
	
	public static final boolean isNotOverridingPublicMethod(IMethodBinding methodBinding) {
		return !isOverriding(methodBinding) && (methodBinding.getModifiers() & Modifier.PUBLIC) != 0;
	}
	
	private void addQuickFixes(String uri, ReconcileProblemImpl problem) {
		
		if (recipeRepo != null && quickfixRegistry != null) {
		
			FixDescriptor fix1 = new FixDescriptor(ID, List.of(uri), LABEL)
	//				.withRangeScope(m.getMarkers().findFirst(Range.class).get()) // TODO create OpenRewrite range
					.withRecipeScope(RecipeScope.NODE);
			
			FixDescriptor fix2 = new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.FILE))
					.withRecipeScope(RecipeScope.FILE);
	
			FixDescriptor fix3 = new FixDescriptor(ID, List.of(uri), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.PROJECT))
					.withRecipeScope(RecipeScope.PROJECT);
			
			QuickfixType quickfixType = quickfixRegistry.getQuickfixType(RewriteRefactorings.REWRITE_RECIPE_QUICKFIX);
	
			if (quickfixType != null && recipeRepo.getRecipe(ID).isPresent()) {
				problem.addQuickfix(new QuickfixData<>(quickfixType, fix1, fix1.getLabel()));
				problem.addQuickfix(new QuickfixData<>(quickfixType, fix2, fix2.getLabel()));
				problem.addQuickfix(new QuickfixData<>(quickfixType, fix3, fix3.getLabel()));
			}
		}
	}
	
}
