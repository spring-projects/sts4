/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.springframework.tooling.jdt.ls.commons.Logger;

/**
 * Helper class to perform a search using Eclipse JDT search engine returning
 * the search results as a Flux.
 * <p>
 * The conversion from Eclipse callback style using {@link SearchRequestor} involves
 * a buffer that allows subscribers to attach to the Flux after the search has already
 * started without loosing results. However, if the buffer overflows then results will
 * be lost.
 * <p>
 * Clients should therfore start consuming the results as soon as possible and avoid
 * blocking the pipeline to avoid the loss of results they may care about.
 * <p>
 * Alternatively, client can specify a large enough buffer size so that the buffer can hold
 * at least as many results as the client may care to retrieve. This will allow the returned
 * Flux to be reused any number of times without timing constraints, provided that the
 * consumer never requests more than the number of buffered results.
 *
 * @author Kris De Volder
 */
public class StreamJdtSearch {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	private SearchEngine engine = new SearchEngine();
	private IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
	private SearchPattern pattern = null;
	private SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
	private Logger logger;
	
	public StreamJdtSearch(Logger logger) {
		this.logger = logger;
	}

	public StreamJdtSearch engine(SearchEngine engine) {
		this.engine = engine;
		return this;
	}

	public StreamJdtSearch scope(IJavaSearchScope scope) {
		this.scope = scope;
		return this;
	}

	public StreamJdtSearch pattern(SearchPattern pattern) {
		this.pattern = pattern;
		return this;
	}

	/**
	 * Create a search scope that includes a given project and its dependencies.
	 */
	public static IJavaSearchScope searchScope(IJavaProject javaProject, boolean includeBinaries, boolean includeSystemLibs) throws JavaModelException {
		int includeMask =
				IJavaSearchScope.REFERENCED_PROJECTS |
				IJavaSearchScope.SOURCES;
		if (includeBinaries) {
			includeMask = includeMask | IJavaSearchScope.APPLICATION_LIBRARIES;
		}
		if (includeSystemLibs) {
			includeMask = includeMask | IJavaSearchScope.SYSTEM_LIBRARIES; 
		}
		return SearchEngine.createJavaSearchScope(new IJavaElement[] {javaProject}, includeMask);
	}

	class Requestor extends SearchRequestor {

		private boolean isCanceled = false;
		private Stream.Builder<SearchMatch> results = Stream.builder();

		public Stream<SearchMatch> asStream() {
			return results.build();
		}

		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			if (isCanceled) {
				debug("!!!! canceling search !!!!");
				//Stop searching
				throw new OperationCanceledException();
			}
			results.add(match);
		}

		public void cancel() {
			isCanceled = true;
		}

	}

	protected SearchEngine searchEngine() {
		return new SearchEngine();
	}

	protected SearchParticipant[] participants() {
		return new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
	}

	public Stream<SearchMatch> search() {
		validate();
		if (scope==null) {
			return Stream.of();
		}
		final Requestor requestor = new Requestor();
		long start = System.currentTimeMillis();
		debug("Starting search for '" + pattern + "'");
		try {
			searchEngine().search(pattern, participants, scope, requestor, new NullProgressMonitor());
		} catch (Exception e) {
			debug("Canceled search for: " + pattern);
			debug("          exception: " + e.getMessage());
			long duration = System.currentTimeMillis() - start;
			debug("          duration: " + duration + " ms");
			requestor.cancel();
		}
		return requestor.asStream();
	}

	private void validate() {
		Assert.isNotNull(engine, "engine");
		//We allow scope to be set to null. This means there's no valid scope (or empty scope)
		// and the search should just return no results.
		//		Assert.isNotNull(scope, "scope");
		Assert.isNotNull(pattern, "pattern");
		Assert.isNotNull(participants, "participants");
	}

	public IJavaSearchScope workspaceScope(boolean includeBinaries) {
		if (includeBinaries) {
			return SearchEngine.createWorkspaceScope();
		} else {
			List<IJavaProject> projects = new ArrayList<>();
			for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				try {
					if (p.isAccessible() && p.hasNature(JavaCore.NATURE_ID)) {
						IJavaProject jp = JavaCore.create(p);
						projects.add(jp);
					}
				} catch (Exception e) {
					logger.log(e);
				}
			}
			int includeMask = IJavaSearchScope.SOURCES;
			return SearchEngine.createJavaSearchScope(projects.toArray(new IJavaElement[projects.size()]), includeMask);
		}
	}

	public static String toWildCardPattern(String query) {
		StringBuilder builder = new StringBuilder("*");
		for (char c : query.toCharArray()) {
			builder.append(c);
			builder.append('*');
		}
		return builder.toString();
	}

	public static SearchPattern toPackagePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.PACKAGE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	public static SearchPattern toClassPattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.CLASS;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	public static SearchPattern toTypePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.TYPE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	
}
