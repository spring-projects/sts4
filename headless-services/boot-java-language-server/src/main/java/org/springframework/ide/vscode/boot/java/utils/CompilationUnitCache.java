package org.springframework.ide.vscode.boot.java.utils;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public final class CompilationUnitCache {

	private static final String GLOB_ALL_JAVA_FILES = "**/*.java";

	private JavaProjectFinder projectFinder;
	private FileObserver fileObserver;
	private ProjectObserver projectObserver;
	private Cache<URI, CompilationUnit> uriToCu;
	private Cache<IJavaProject, Set<URI>> projectToDocs;
	private String fileChangeSubscription;
	private String fileDeletedSubscription;
	private ProjectObserver.Listener projectListener;

	private ReadLock readLock;
	private WriteLock writeLock;

	public CompilationUnitCache(JavaProjectFinder projectFinder, FileObserver fileObserver, ProjectObserver projectObserver) {
		this.projectFinder = projectFinder;
		this.fileObserver = fileObserver;
		this.projectObserver = projectObserver;
		projectListener = new CUProjectListener();

		uriToCu = CacheBuilder.newBuilder().build();
		projectToDocs = CacheBuilder.newBuilder().build();

		if (this.fileObserver != null) {
			fileChangeSubscription = this.fileObserver.onFileChanged(Collections.singletonList(GLOB_ALL_JAVA_FILES), (uri) -> invalidateCuForJavaFile(uri));
			fileDeletedSubscription = this.fileObserver.onFileDeleted(Collections.singletonList(GLOB_ALL_JAVA_FILES), (uri) -> invalidateCuForJavaFile(uri));
		}

		if (this.projectObserver != null) {
			this.projectObserver.addListener(projectListener);
		}

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
	}

	public void dispose() {
		if (fileObserver != null) {
			fileObserver.unsubscribe(fileChangeSubscription);
			fileObserver.unsubscribe(fileDeletedSubscription);
		}
		if (projectObserver != null) {
			projectObserver.removeListener(projectListener);
		}
	}

	public CompilationUnit getCompilationUnit(TextDocument document) throws Exception {
		URI uri = URI.create(document.getUri());
		readLock.lock();
		try {
			return uriToCu.get(uri, () -> parse(document));
		} finally {
			readLock.unlock();
		}
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
		ASTParser parser = ASTParser.newParser(AST.JLS8);
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

	private CompilationUnit parse(TextDocument document)
			throws Exception, BadLocationException {
		IJavaProject project = projectFinder.find(document.getId()).orElse(null);
		CompilationUnit cu = parse(document, project);
		if (project != null) {
			projectToDocs.get(project, () -> new HashSet<>()).add(URI.create(document.getUri()));
		}
		return cu;
	}

	private static String[] getClasspathEntries(TextDocument document, IJavaProject project) throws Exception {
		if (project == null) {
			return new String[0];
		} else {
			IClasspath classpath = project.getClasspath();
			Stream<Path> classpathEntries = classpath.getClasspathEntries();
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
