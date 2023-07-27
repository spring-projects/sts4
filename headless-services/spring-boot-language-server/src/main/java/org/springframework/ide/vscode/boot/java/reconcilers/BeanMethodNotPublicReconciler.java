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
import java.util.UUID;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.openrewrite.java.spring.BeanMethodsNotPublic;
import org.openrewrite.marker.Range;
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
import org.springframework.ide.vscode.commons.util.BadLocationException;
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
			MethodDeclaration method = (MethodDeclaration) node.getParent();
			Version version = SpringProjectUtil.getDependencyVersion(project, SpringProjectUtil.SPRING_BOOT);
			
			if (version.getMajor() >= 2) {
				IMethodBinding methodBinding = method.resolveBinding();
				if (isNotOverridingPublicMethod(methodBinding)) {
					
					ReconcileProblemImpl problem = ((List<?>)method.modifiers()).stream()
							.filter(Modifier.class::isInstance)
							.map(Modifier.class::cast)
							.filter(modifier -> modifier.isPublic())
							.findFirst()
							.map(modifier -> new ReconcileProblemImpl(
									Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD, Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD.getLabel(),
									modifier.getStartPosition(), modifier.getLength()))
							.orElse(new ReconcileProblemImpl(
									Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD, Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD.getLabel(),
									method.getName().getStartPosition(), method.getName().getLength()));

					addQuickFixes(doc, problem, method);
					
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
	
	private void addQuickFixes(IDocument doc, ReconcileProblemImpl problem, MethodDeclaration method) {
		
		if (recipeRepo != null && quickfixRegistry != null) {
		
			FixDescriptor fix1 = new FixDescriptor(ID, List.of(doc.getUri()), LABEL)
					.withRecipeScope(RecipeScope.NODE);

			try {
				Range methodRange = createOpenRewriteRange(doc, method);
				fix1 = fix1.withRangeScope(methodRange);
			}
			catch (BadLocationException e) {
				log.warn("bad location happened while calculating method range for " + method.toString(), e);
			}
			

			FixDescriptor fix2 = new FixDescriptor(ID, List.of(doc.getUri()), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.FILE))
					.withRecipeScope(RecipeScope.FILE);
	
			FixDescriptor fix3 = new FixDescriptor(ID, List.of(doc.getUri()), RecipeCodeActionDescriptor.buildLabel(LABEL, RecipeScope.PROJECT))
					.withRecipeScope(RecipeScope.PROJECT);
			
			
			QuickfixType quickfixType = quickfixRegistry.getQuickfixType(RewriteRefactorings.REWRITE_RECIPE_QUICKFIX);
	
			if (quickfixType != null /* && recipeRepo.getRecipe(ID).isPresent()*/ ) { // recipes load async, so they might not be around when this validation runs
				// in addition to that we assume and ship those recipes with the language server anyway, so we can assume they are around

				problem.addQuickfix(new QuickfixData<>(quickfixType, fix1, fix1.getLabel()));
				problem.addQuickfix(new QuickfixData<>(quickfixType, fix2, fix2.getLabel()));
				problem.addQuickfix(new QuickfixData<>(quickfixType, fix3, fix3.getLabel()));
			}
		}
	}

	private Range createOpenRewriteRange(IDocument doc, MethodDeclaration method) throws BadLocationException {
		
		int startOffset = method.getStartPosition();
		int endOffset = method.getStartPosition() + method.getLength();

		int startLine = doc.getLineOfOffset(startOffset);
		int endLine = doc.getLineOfOffset(endOffset);
		
		int startColumn = startOffset - doc.getLineOffset(startLine);
		int endColumn = endOffset - doc.getLineOffset(endLine);
		
		Range.Position startPosition = new Range.Position(startOffset, startLine, startColumn);
		Range.Position endPosition = new Range.Position(endOffset, endLine, endColumn);
		
		return new Range(UUID.randomUUID(), startPosition, endPosition);
	}
	
}
