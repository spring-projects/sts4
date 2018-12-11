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
package org.springframework.ide.vscode.commons.jandex;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.jandex.JandexIndex.JavadocProviderFactory;
import org.springframework.ide.vscode.commons.java.ClasspathIndex;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.languageserver.java.ls.Classpath;
import org.springframework.ide.vscode.commons.languageserver.java.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.FileObserver;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * Classpath with Jandex Java index for searching types
 *
 * @author Alex Boyko
 *
 */
public final class JandexClasspath implements ClasspathIndex {

	public static final Logger log = LoggerFactory.getLogger(JandexClasspath.class);

	public enum JavadocProviderTypes {
//		JAVA_PARSER, //Used to be based on githb java parser. If need something back that can extract docs from source code, we have to implement
						// based on JDT parser. But at the moment this wasn't being used so just got removed.
		HTML
	}

	private Supplier<JandexIndex> javaIndex;
	private final IClasspath classpath;
	private final FileObserver fileObserver;
	private final JavadocProviderFactory javadocProviderFactory;

	public JandexClasspath(IClasspath classpath, FileObserver fileObserver, JavadocProviderFactory javadocProviderFactory) {
		this.fileObserver = fileObserver;
		this.classpath = classpath;
		this.javadocProviderFactory = javadocProviderFactory;
		this.javaIndex = Suppliers.synchronizedSupplier(Suppliers.memoize(() -> createIndex()));
	}

	protected JandexIndex createIndex() {
		log.info("Creating JandexIndex for "+classpath.getName());
		attachFolderListeners();
		return new JandexIndex(classpath, jarFile -> findIndexFile(jarFile), javadocProviderFactory);
	}

	private Disposable.Composite subscriptions = Disposables.composite();

	private void attachFolderListeners() {
		synchronized (this) {
			subscriptions.dispose();
			subscriptions = Disposables.composite();
		}
		for (File cpe : IClasspathUtil.getBinaryRoots(classpath, Classpath::isSource)) {
			final List<String> rebuildGlobPattern = Arrays.asList(cpe.toString().replace(File.separator, "/") + "/**/*.class");
			Disposable disposable = fileObserver.onAnyChange(rebuildGlobPattern, (uri) -> reindex());
			subscriptions.add(disposable);
		}
	}

	@Override
	public void dispose() {
		Composite toDispose = subscriptions;
		subscriptions = null;
		if (toDispose!=null) {
			toDispose.dispose();
		}
	}

	@Override
	public Optional<URL> sourceContainer(File binaryClasspathRoot) {
		CPE cpe = IClasspathUtil.findEntryForBinaryRoot(classpath, binaryClasspathRoot);
		return cpe == null ? Optional.empty() : Optional.ofNullable(cpe.getSourceContainerUrl());
	}

	@Override
	public IType findType(String fqName) {
		return javaIndex.get().findType(fqName);
	}

	@Override
	public Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, Predicate<IType> typeFilter) {
		return javaIndex.get().fuzzySearchTypes(searchTerm, typeFilter);
	}

	@Override
	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm) {
		return javaIndex.get().fuzzySearchPackages(searchTerm);
	}

	@Override
	public Flux<IType> allSubtypesOf(IType type) {
		return javaIndex.get().allSubtypesOf(type);
	}

	private File findIndexFile(File jarFile) {
		File indexFolder = getIndexFolder();
		if (indexFolder == null) {
			return null;
		}
		return new File(indexFolder.toString(), jarFile.getName() + "-" + jarFile.lastModified() + ".jdx");
	}

	protected File getIndexFolder() {
		return JandexIndex.getIndexFolder();
	}

	@Override
	public IJavaModuleData findClasspathResourceContainer(String fqName) {
		return javaIndex.get().findClasspathResourceForType(fqName);
	}

	private void reindex() {
		log.info("Clearing JandexIndex for "+classpath.getName());
		this.javaIndex = Suppliers.synchronizedSupplier(Suppliers.memoize(() -> createIndex()));
	}

	@Override
	public ImmutableList<String> getClasspathResources() {
		return IClasspathUtil.getSourceFolders(classpath)
		.flatMap(folder -> {
			try {
				return Files.walk(folder.toPath())
						.filter(path -> Files.isRegularFile(path))
						.map(path -> folder.toPath().relativize(path))
						.map(relativePath -> relativePath.toString())
						.filter(pathString -> !pathString.endsWith(".java") && !pathString.endsWith(".class"));
			} catch (IOException e) {
				return Stream.empty();
			}
		})
		.collect(CollectorUtil.toImmutableList());
	}

	private static void updateQueue(Queue<String> queue, Set<String> exclusion, IType type) {
		for (String t : type.getSuperInterfaceNames()) {
			if (!exclusion.contains(t)) {
				queue.add(t);
				exclusion.add(t);
			}
		}
		String superClass = type.getSuperclassName();
		if (superClass != null && !exclusion.contains(superClass)) {
			queue.add(superClass);
			exclusion.add(superClass);
		}
	}

	@Override
	public Flux<IType> allSuperTypesOf(IType type) {
		Queue<String> queue = new LinkedList<>();
		HashSet<String> visited = new HashSet<>();
		updateQueue(queue, visited, type);
		return Flux.generate(() -> queue, (state, sink) -> {
			IType nextType = null;
			while (nextType == null && state.peek() != null) {
				String typeName = state.poll();
				nextType = findType(typeName);
				if (nextType != null) {
					sink.next(nextType);
					updateQueue(state, visited, nextType);
				}
			}
			if (state.peek() == null) {
				sink.complete();
			}
			return state;
		});
	}

}
