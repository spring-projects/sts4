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
package org.springframework.ide.vscode.boot.java.rewrite;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.marker.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.SpringJavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.JavaReconciler;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings.Data;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.RecipeSpringJavaProblemDescriptor;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix.QuickfixData;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class RewriteReconciler implements JavaReconciler {
	
	private static final Logger log = LoggerFactory.getLogger(RewriteReconciler.class);
	
	private RewriteCompilationUnitCache cuCache;

	private QuickfixRegistry quickfixRegistry;

	private RewriteRecipeRepository recipeRepo;
	
	public RewriteReconciler(RewriteRecipeRepository recipeRepo, RewriteCompilationUnitCache cuCache, QuickfixRegistry quickfixRegistry) {
		this.recipeRepo = recipeRepo;
		this.cuCache = cuCache;
		this.quickfixRegistry = quickfixRegistry;
	}

	@Override
	public void reconcile(IJavaProject project, IDocument doc, IProblemCollector problemCollector) {
		try {
			problemCollector.beginCollecting();
			
			// Wait for recipe repo to load if not loaded - should be loaded by the time we get here.
			recipeRepo.loaded.get();
			
			List<RecipeSpringJavaProblemDescriptor> descriptors = recipeRepo.getProblemRecipeDescriptors().stream()
				.filter(d -> d.isApplicable(project))
				.collect(Collectors.toList());
			
			if (!descriptors.isEmpty()) {
				CompilationUnit cu = cuCache.getCU(project, URI.create(doc.getUri()));
				
				if (cu != null) {
					
					cu = recipeRepo.mark(descriptors, cu);
					
					new JavaIsoVisitor<ExecutionContext>() {
						
			            @Override
			            public @Nullable J visit(@Nullable Tree tree, ExecutionContext context) {
							J t = super.visit(tree, context);
			            	if (t instanceof J) {
			            		List<FixAssistMarker> markers = t.getMarkers().findAll(FixAssistMarker.class);
			            		for (FixAssistMarker m : markers) {
									for (ReconcileProblem problem : createProblems(doc, m, t)) {
										problemCollector.accept(problem);
									}
			            		}
			            	}
			            	return t;
			            }

					}.visit(cu, new InMemoryExecutionContext(e -> log.error("", e)));
				}
			}			
			
		} catch (Exception e) {
			log.error("", e);
		} finally {
			problemCollector.endCollecting();
		}		
	}

	private List<ReconcileProblem> createProblems(IDocument doc, FixAssistMarker m, J astNode) {
		if (astNode != null) {
			Range range = astNode.getMarkers().findFirst(Range.class).orElse(null);
			if (range != null) {
				RecipeSpringJavaProblemDescriptor recipeFixDescriptor = recipeRepo.getProblemRecipeDescriptor(m.getRecipeId());
				if (recipeFixDescriptor != null && recipeFixDescriptor.getScopes() != null && recipeRepo.getRecipe(recipeFixDescriptor.getRecipeId()).isPresent()) {
					return Arrays.stream(recipeFixDescriptor.getScopes()).map(s -> createProblemFromScope(doc, recipeFixDescriptor, s, m, range)).collect(Collectors.toList());
				}
			}
		}
		return Collections.emptyList();
	}
	
	private ReconcileProblemImpl createProblemFromScope(IDocument doc, RecipeSpringJavaProblemDescriptor recipeFixDescriptor, RecipeScope s,
			FixAssistMarker m, Range range) {
		SpringJavaProblemType problemType = recipeFixDescriptor.getProblemType();
		ReconcileProblemImpl problem = new ReconcileProblemImpl(problemType, problemType.getLabel(), range.getStart().getOffset(), range.getEnd().getOffset() - range.getStart().getOffset());
		QuickfixType quickfixType = quickfixRegistry.getQuickfixType(m.getRecipeId());
		if (quickfixType != null) {
			problem.addQuickfix(new QuickfixData<>(
					quickfixType,
					new Data(m.getRecipeId(), doc.getUri(), s, m.getScope().toString(), m.getParameters()),
					recipeFixDescriptor.getLabel(s)
			));
		}
		return problem;
	}
		
}
