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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class JdtReconciler implements JavaReconciler {
	
	private static final Logger log = LoggerFactory.getLogger(JdtReconciler.class);
	
	// annotations with SpEL expression params 
	public static final String SPRING_CACHEABLE = "org.springframework.cache.annotation.Cacheable";
	public static final String SPRING_CACHE_EVICT = "org.springframework.cache.annotation.CacheEvict";
	
	public static final String SPRING_EVENT_LISTENER = "org.springframework.context.event.EventListener";
	
	public static final String SPRING_PRE_AUTHORIZE = "org.springframework.security.access.prepost.PreAuthorize";
	public static final String SPRING_PRE_FILTER = "org.springframework.security.access.prepost.PreFilter";
	public static final String SPRING_POST_AUTHORIZE = "org.springframework.security.access.prepost.PostAuthorize";
	public static final String SPRING_POST_FILTER= "org.springframework.security.access.prepost.PostFilter";
	
	public static final String SPRING_CONDITIONAL_ON_EXPRESSION = "org.springframework.boot.autoconfigure.condition.ConditionalOnExpression";
	
	private final CompilationUnitCache compilationUnitCache;
	private final JdtAstReconciler[] reconcilers;
	private BootJavaConfig config;

	public JdtReconciler(CompilationUnitCache compilationUnitCache, BootJavaConfig config, JdtAstReconciler[] reconcilers) {
		this.compilationUnitCache = compilationUnitCache;
		this.config = config;
		this.reconcilers = reconcilers;
	}

	@Override
	public void reconcile(IJavaProject project, final IDocument doc, final IProblemCollector problemCollector) {
		final long s = System.currentTimeMillis();
		URI uri = URI.create(doc.getUri());
		compilationUnitCache.withCompilationUnit(project, uri, cu -> {
			if (cu != null) {
				try {
					reconcile(project, URI.create(doc.getUri()), cu, problemCollector, true);
				} catch (RequiredCompleteAstException e) {
					log.error("Unexpected incomplete AST", e);
				}
			}
			log.info("reconciling (JDT): " + doc.getUri() + " done in " + (System.currentTimeMillis() - s) + "ms");
			return null;
		});
	}
	

	public void reconcile(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector, boolean isCompleteAst) throws RequiredCompleteAstException {
		for (JdtAstReconciler reconciler : getApplicableReconcilers(project)) {
			try {
				reconciler.reconcile(project, docUri, cu, problemCollector, isCompleteAst);
			} catch (RequiredCompleteAstException e) {
				throw e;
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	private List<JdtAstReconciler> getApplicableReconcilers(IJavaProject project) {
		List<JdtAstReconciler> applicableReconcilers = new ArrayList<>(reconcilers.length);
		for (JdtAstReconciler r : reconcilers) {
			switch (config.getProblemApplicability(r.getProblemType())) {
			case ON:
				if (SpringProjectUtil.isBootProject(project)) {
					applicableReconcilers.add(r);
				}
				break;
			case OFF:
				break;
			default: // AUTO
				if (r.isApplicable(project)) {
					applicableReconcilers.add(r);
				}
			}
		}
		return applicableReconcilers;
	}


	@Override
	public Map<IDocument, Collection<ReconcileProblem>> reconcile(IJavaProject project, List<TextDocument> docs,
			Runnable incrementProgress) {
		return Collections.emptyMap();
	}
	
	
}
