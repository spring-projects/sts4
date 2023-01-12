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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.marker.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.java.reconcilers.JavaReconciler;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix.QuickfixData;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.ORAstUtils;
import org.springframework.ide.vscode.commons.rewrite.maven.MavenProjectParser;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class RewriteReconciler implements JavaReconciler {
	
	private static final Logger log = LoggerFactory.getLogger(RewriteReconciler.class);
	
	private RewriteCompilationUnitCache cuCache;
	private QuickfixRegistry quickfixRegistry;
	private RewriteRecipeRepository recipeRepo;
	private BootJavaConfig config;

	public RewriteReconciler(RewriteRecipeRepository recipeRepo, RewriteCompilationUnitCache cuCache, QuickfixRegistry quickfixRegistry, BootJavaConfig config) {
		this.recipeRepo = recipeRepo;
		this.cuCache = cuCache;
		this.quickfixRegistry = quickfixRegistry;
		this.config = config;
	}

	@Override
	public void reconcile(IJavaProject project, IDocument doc, IProblemCollector problemCollector) {

		if (!config.isRewriteReconcileEnabled()) {
			return;
		}

		log.info("reconciling (OpenRewrite): " + project.getElementName() + " - " + doc.getUri());
		long start = System.currentTimeMillis();

		try {
			problemCollector.beginCollecting();
			
			List<RecipeCodeActionDescriptor> descriptors = getProblemRecipeDescriptors(project);
			
			if (!descriptors.isEmpty()) {
				CompilationUnit cu = cuCache.getCU(project, URI.create(doc.getUri()));
				if (cu != null) {
					collectProblems(descriptors, doc, cu, problemCollector::accept);
				}
			}			
		} catch (Exception e) {
			if (ORAstUtils.isExceptionFromInterrupedThread(e)) {
				log.debug("", e);
			} else {
				log.error("", e);
			}
		} finally {
			problemCollector.endCollecting();
		}	
		
		long end = System.currentTimeMillis();
		log.info("reconciling (OpenRewrite): " + project.getElementName() + " done in " + (end - start) + "ms");
	}
	
	private List<ReconcileProblem> createProblems(IDocument doc, FixAssistMarker m, J astNode) {
		if (astNode != null) {
			Range range = astNode.getMarkers().findFirst(Range.class).orElse(null);
			if (range != null) {
				RecipeCodeActionDescriptor recipeFixDescriptor = recipeRepo.getCodeActionRecipeDescriptor(m.getDescriptorId());
				if (recipeFixDescriptor != null) {
					return List.of(createProblem(doc, recipeFixDescriptor, m, range));
				}
			}
		}
		return Collections.emptyList();
	}
	
	private ReconcileProblemImpl createProblem(IDocument doc, RecipeCodeActionDescriptor recipeFixDescriptor,
			FixAssistMarker m, Range range) {
		ProblemType problemType = recipeFixDescriptor.getProblemType();
		ReconcileProblemImpl problem = new ReconcileProblemImpl(problemType, m.getLabel() == null ? problemType.getLabel() : m.getLabel(), range.getStart().getOffset(), range.getEnd().getOffset() - range.getStart().getOffset());
		QuickfixType quickfixType = quickfixRegistry.getQuickfixType(RewriteRefactorings.REWRITE_RECIPE_QUICKFIX);
		if (quickfixType != null) {
			for (FixDescriptor f : m.getFixes()) {
				if (recipeRepo.getRecipe(f.getRecipeId()).isPresent()) {
					problem.addQuickfix(new QuickfixData<>(
							quickfixType,
							f,
							f.getLabel()
					));
				}
			}
		}
		return problem;
	}

	@Override
	public Map<IDocument, Collection<ReconcileProblem>> reconcile(IJavaProject project, List<TextDocument> docs,
			Function<TextDocument, IProblemCollector> problemCollectorFactory) {
		
		log.info("reconciling (OpenRewrite, multiple docs): " + project.getElementName() + " - " + docs.size());
		long start = System.currentTimeMillis();

		Map<IDocument, Collection<ReconcileProblem>> allProblems = new HashMap<>();
		List<Path> testSourceFolders = IClasspathUtil.getProjectTestJavaSources(project.getClasspath()).map(f -> f.toPath()).collect(Collectors.toList());
		List<TextDocument> testSources = new ArrayList<>(docs.size());
		List<TextDocument> mainSources = new ArrayList<>(docs.size());
		for (TextDocument d : docs) {
			Path p = Paths.get(URI.create(d.getUri()));
			if (testSourceFolders.stream().anyMatch(t -> p.startsWith(t))) {
				testSources.add(d);
			} else {
				mainSources.add(d);
			}
		}
		JavaParser javaParser = ORAstUtils.createJavaParser(project);
		javaParser.setSourceSet(MavenProjectParser.MAIN);
		allProblems.putAll(doReconcile(project, mainSources, problemCollectorFactory, javaParser));
		javaParser.setSourceSet(MavenProjectParser.TEST);
		allProblems.putAll(doReconcile(project, testSources, problemCollectorFactory, javaParser));
		
		long end = System.currentTimeMillis();
		log.info("reconciling (OpenRewrite, multiple docs): " + project.getElementName() + " - " + docs.size() + " done in " + (end - start) + "ms");
		
		System.gc();

		return allProblems;
	}
	
	// Parse all at once	
//	private Map<IDocument, Collection<ReconcileProblem>> doReconcile(IJavaProject project, List<TextDocument> docs,
//			Function<TextDocument, IProblemCollector> problemCollectorFactory, JavaParser javaParser) {
//		Map<IDocument, Collection<ReconcileProblem>> allProblems = new HashMap<>();
//		
//		if (javaParser != null && config.isRewriteReconcileEnabled()) {
//			try {
//				List<RecipeCodeActionDescriptor> descriptors = getProblemRecipeDescriptors(project);
//
//				List<CompilationUnit> cus = ORAstUtils.parseInputs(javaParser,
//						docs.stream().map(d -> new Parser.Input(Paths.get(URI.create(d.getUri())), () -> {
//							return new ByteArrayInputStream(d.get().getBytes());
//						})).collect(Collectors.toList()));
//
//				if (!descriptors.isEmpty()) {
//
//					for (int i = 0; i < cus.size(); i++) {
//						final IDocument doc = docs.get(i);
//						List<ReconcileProblem> problems = new ArrayList<>();
//						collectProblems(descriptors, doc, cus.get(i), problems::add);
//						if (!problems.isEmpty()) {
//							allProblems.put(doc, problems);
//						}
//					}
//				}
//			} catch (Exception e) {
//				if (ORAstUtils.isExceptionFromInterrupedThread(e)) {
//					log.debug("", e);
//				} else {
//					log.error("", e);
//				}
//			}
//		}
//		return allProblems;
//	}
	
	// Parse One-by-one and share the parser
//	private Map<IDocument, Collection<ReconcileProblem>> doReconcile(IJavaProject project, List<TextDocument> docs,
//			Function<TextDocument, IProblemCollector> problemCollectorFactory, JavaParser javaParser) {
//		Map<IDocument, Collection<ReconcileProblem>> allProblems = new HashMap<>();
//		
//		if (javaParser != null && config.isRewriteReconcileEnabled()) {
//			try {
//				List<RecipeCodeActionDescriptor> descriptors = getProblemRecipeDescriptors(project);
//
//				if (!descriptors.isEmpty()) {
//
//					for (IDocument doc : docs) {
//						List<ReconcileProblem> problems = new ArrayList<>();
//						CompilationUnit source = ORAstUtils.parseInputs(javaParser, List.of(new Parser.Input(Paths.get(URI.create(doc.getUri())), () -> {
//							return new ByteArrayInputStream(doc.get().getBytes());
//						}))).get(0);
//						collectProblems(descriptors, doc, source, problems::add);
//						if (!problems.isEmpty()) {
//							allProblems.put(doc, problems);
//						}
//					}
//				}
//			} catch (Exception e) {
//				if (ORAstUtils.isExceptionFromInterrupedThread(e)) {
//					log.debug("", e);
//				} else {
//					log.error("", e);
//				}
//			}
//		}
//		return allProblems;
//	}
	
	private static final int BATCH = 10;

	// Parse in batches and share the parser
	private Map<IDocument, Collection<ReconcileProblem>> doReconcile(IJavaProject project, List<TextDocument> docs,
			Function<TextDocument, IProblemCollector> problemCollectorFactory, JavaParser javaParser) {
		Map<IDocument, Collection<ReconcileProblem>> allProblems = new HashMap<>();
		if (javaParser != null && config.isRewriteReconcileEnabled()) {
			try {
				List<RecipeCodeActionDescriptor> descriptors = getProblemRecipeDescriptors(project);


				if (!descriptors.isEmpty()) {

					for (int i = 0; i < docs.size(); i += BATCH) {
						List<TextDocument> batchList = docs.subList(i, Math.min(i + BATCH, docs.size()));
						
						List<CompilationUnit> cus = ORAstUtils.parseInputs(javaParser,
								batchList.stream().map(d -> new Parser.Input(Paths.get(URI.create(d.getUri())), () -> {
									return new ByteArrayInputStream(d.get().getBytes());
								})).collect(Collectors.toList()));
						
						/*
						 * If exception occurs during parsing inputs the list of inputs would become shorter than the list of corresponding documents
						 */
						
						for (int j = 0, k = 0; j < batchList.size() && k < cus.size(); j++) {
							final IDocument doc = batchList.get(j);
							List<ReconcileProblem> problems = new ArrayList<>();
							CompilationUnit cu = cus.get(k);
							Path sourcePath = Paths.get(URI.create(doc.getUri()));
							if (cu.getSourcePath().equals(sourcePath)) {
								k++;
								collectProblems(descriptors, doc, cu, problems::add);
								if (!problems.isEmpty()) {
									allProblems.put(doc, problems);
								}
							} else {
								log.warn("(OpenRewrite) Failed to parse source for " + sourcePath);
							}
						}

					}
				}
			} catch (Exception e) {
				if (ORAstUtils.isExceptionFromInterrupedThread(e)) {
					log.debug("", e);
				} else {
					log.error("", e);
				}
			}
		}
		return allProblems;
	}

	private List<RecipeCodeActionDescriptor> getProblemRecipeDescriptors(IJavaProject project)
			throws InterruptedException, ExecutionException {
		return recipeRepo.getProblemRecipeDescriptors().stream().filter(d -> d.getProblemType() != null).filter(d -> {
			switch (config.getProblemApplicability(d.getProblemType())) {
			case ON:
				return SpringProjectUtil.isBootProject(project);
			case OFF:
				return false;
			default: // AUTO
				return d.isApplicable(project);
			}
		}).collect(Collectors.toList());
	}
	
	private void collectProblems(List<RecipeCodeActionDescriptor> descriptors, IDocument doc, CompilationUnit compilationUnit, Consumer<ReconcileProblem> problemHandler) {
		CompilationUnit cu = recipeRepo.mark(descriptors, compilationUnit);
		
		new JavaIsoVisitor<ExecutionContext>() {
			
            @Override
            public J visit(Tree tree, ExecutionContext context) {
				J t = super.visit(tree, context);
            	if (t instanceof J) {
            		List<FixAssistMarker> markers = t.getMarkers().findAll(FixAssistMarker.class);
            		for (FixAssistMarker m : markers) {
						for (ReconcileProblem problem : createProblems(doc, m, t)) {
							problemHandler.accept(problem);
						}
            		}
            	}
            	return t;
            }

		}.visit(cu, new InMemoryExecutionContext(e -> log.error("", e)));

	}
		
}
