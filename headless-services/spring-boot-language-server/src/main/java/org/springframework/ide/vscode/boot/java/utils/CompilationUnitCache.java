/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public final class CompilationUnitCache implements DocumentContentProvider {

	private static final Logger logger = LoggerFactory.getLogger(CompilationUnitCache.class);

	private static final long CU_ACCESS_EXPIRATION = 1;
	private JavaProjectFinder projectFinder;
	private ProjectObserver projectObserver;
	private Cache<URI, CompilationUnit> uriToCu;
	private Cache<IJavaProject, Set<URI>> projectToDocs;
	private ProjectObserver.Listener projectListener;
	private SimpleTextDocumentService documents;

	private ReadLock readLock;
	private WriteLock writeLock;

	public CompilationUnitCache(JavaProjectFinder projectFinder, SimpleTextDocumentService documents, ProjectObserver projectObserver) {
		this.projectFinder = projectFinder;
		this.projectObserver = projectObserver;
		this.documents = documents;
		projectListener = ProjectObserver.onAny(this::invalidateProject);

		// PT 154618835 - Avoid retaining the CU in the cache as it consumes memory if it hasn't been
		// accessed after some time
		uriToCu = CacheBuilder.newBuilder()
				.expireAfterWrite(CU_ACCESS_EXPIRATION, TimeUnit.MINUTES)
				.build();
		projectToDocs = CacheBuilder.newBuilder().build();

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();

		if (documents != null) {
			documents.onDidChangeContent(doc -> invalidateCuForJavaFile(doc.getDocument().getId().getUri()));
			documents.onDidClose(doc -> invalidateCuForJavaFile(doc.getId().getUri()));
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
	@Deprecated
	public <T> T withCompilationUnit(TextDocument document, Function<CompilationUnit, T> requestor) {
		IJavaProject project = projectFinder.find(document.getId()).orElse(null);
		URI uri = URI.create(document.getUri());
		return withCompilationUnit(project, uri, requestor);
	}

	public <T> T withCompilationUnit(IJavaProject project, URI uri, Function<CompilationUnit, T> requestor) {
		if (project != null) {

			readLock.lock();
			CompilationUnit cu = null;

			try {
				cu = uriToCu.get(uri, () -> {
					CompilationUnit cUnit = parse(uri.toString(), fetchContent(uri).toCharArray(), project);
					projectToDocs.get(project, () -> new HashSet<>()).add(uri);
					return cUnit;
				});
				if (cu != null) {
					projectToDocs.get(project, () -> new HashSet<>()).add(uri);
				}
			} catch (Exception e) {
				logger.error("", e);
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
					logger.error("", e);
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
		String[] classpathEntries = getClasspathEntries(project);
		String docURI = document.getUri();
		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		char[] source = document.get(0, document.getLength()).toCharArray();
		return parse(source, docURI, unitName, classpathEntries);
	}

	public static CompilationUnit parse(String uri, char[] source, IJavaProject project) throws Exception {
		String[] classpathEntries = getClasspathEntries(project);
		String unitName = uri.substring(uri.lastIndexOf("/"));
		return parse(source, uri, unitName, classpathEntries);
	}

	public static CompilationUnit parse(char[] source, String docURI, String unitName, String[] classpathEntries) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS11);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_11, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);

		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, false);

		parser.setUnitName(unitName);
		parser.setSource(source);

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		return cu;
	}

	private static String[] getClasspathEntries(IJavaProject project) throws Exception {
		if (project == null) {
			return new String[0];
		} else {
			IClasspath classpath = project.getClasspath();
			Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();
			return classpathEntries
					.filter(file -> file.exists())
					.map(file -> file.getAbsolutePath()).toArray(String[]::new);
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

	@Override
	public String fetchContent(URI uri) throws Exception {
		if (documents != null) {
			TextDocument document = documents.get(uri.toString());
			if (document != null) {
				return document.get(0, document.getLength());
			}
		}
		return IOUtils.toString(uri);

	}
}
