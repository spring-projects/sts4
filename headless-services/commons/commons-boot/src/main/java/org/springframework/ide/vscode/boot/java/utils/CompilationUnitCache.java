/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public final class CompilationUnitCache {

	private JavaProjectFinder projectFinder;
	private ProjectObserver projectObserver;
	private Cache<URI, CompilationUnit> uriToCu;
	private Cache<IJavaProject, Set<URI>> projectToDocs;
	private ProjectObserver.Listener projectListener;

	private ReadLock readLock;
	private WriteLock writeLock;

	public CompilationUnitCache(JavaProjectFinder projectFinder, SimpleTextDocumentService documentService, ProjectObserver projectObserver) {
		this.projectFinder = projectFinder;
		this.projectObserver = projectObserver;
		projectListener = new CUProjectListener();

		uriToCu = CacheBuilder.newBuilder().build();
		projectToDocs = CacheBuilder.newBuilder().build();

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();

		if (documentService != null) {
			documentService.onDidChangeContent(doc -> invalidateCuForJavaFile(doc.getDocument().getId().getUri()));
			documentService.onDidClose(doc -> invalidateCuForJavaFile(doc.getId().getUri()));
		}

		if (this.projectObserver != null) {
			this.projectObserver.addListener(projectListener);
		}
	}

	public void dispose() {
		if (projectObserver != null) {
			projectObserver.removeListener(projectListener);
		}
	}

	/**
	 * Retrieves a CompiationUnitn AST from the cache and passes it to a requestor callback, applying
	 * proper thread synchronization around the requestor.
	 * <p>
	 * Warning: Callers should take care to do all AST processing inside of the requestor callback and
	 * not pass of AST nodes to helper functions that work aynchronously or store AST nodes or ITypeBindings
	 * for later use. The JDT ASTs are not thread safe!
	 */
	public <T> T withCompilationUnit(TextDocument document, Function<CompilationUnit, T> requestor) {
		URI uri = URI.create(document.getUri());
		IJavaProject project = projectFinder.find(document.getId()).orElse(null);

		if (project != null) {

			readLock.lock();
			CompilationUnit cu = null;

			try {
				cu = uriToCu.get(uri, () -> {
					CompilationUnit cUnit = parse(document, project);
					projectToDocs.get(project, () -> new HashSet<>()).add(URI.create(document.getUri()));
					return cUnit;
				});
				if (cu != null) {
					projectToDocs.get(project, () -> new HashSet<>()).add(URI.create(document.getUri()));
				}
			} catch (Exception e) {
				Log.log(e);
			} finally {
				readLock.unlock();
			}

			if (cu != null) {
				try {
					synchronized (cu.getAST()) {
						return requestor.apply(cu);
					}
				}
				catch (Exception e) {
					Log.log(e);
				}
			}
		}

		return requestor.apply(null);
	}

	private void invalidateCuForJavaFile(String uriStr) {
		URI uri = URI.create(uriStr);
		writeLock.lock();
		try {
			uriToCu.invalidate(uri);
		} finally {
			writeLock.unlock();
		}
	}

	public static CompilationUnit parse(TextDocument document, IJavaProject project) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS9);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);

		String[] classpathEntries = getClasspathEntries(document, project);
		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		String docURI = document.getUri();
		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		parser.setSource(document.get(0, document.getLength()).toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		return cu;
	}

	private static String[] getClasspathEntries(TextDocument document, IJavaProject project) throws Exception {
		if (project == null) {
			return new String[0];
		} else {
			IClasspath classpath = project.getClasspath();
			Stream<Path> classpathEntries = classpath.getClasspathEntries().stream();
			return classpathEntries
					.filter(path -> path.toFile().exists())
					.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
		}
	}

	private void invalidateProject(IJavaProject project) {
		Set<URI> docUris = projectToDocs.getIfPresent(project);
		if (docUris != null) {
			writeLock.lock();
			try {
				uriToCu.invalidateAll(docUris);
				projectToDocs.invalidate(project);
			} finally {
				writeLock.unlock();
			}
		}
	}

	private class CUProjectListener implements ProjectObserver.Listener {

		@Override
		public void created(IJavaProject project) {
		}

		@Override
		public void changed(IJavaProject project) {
			invalidateProject(project);
		}

		@Override
		public void deleted(IJavaProject project) {
			invalidateProject(project);
		}

	}
}
