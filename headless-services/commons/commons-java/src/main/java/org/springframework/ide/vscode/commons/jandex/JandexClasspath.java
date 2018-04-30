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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.java.ClasspathIndex;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.JavaDocProviders;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.util.CollectorUtil;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.Log;

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

	public static JavadocProviderTypes providerType = JavadocProviderTypes.HTML;

	public enum JavadocProviderTypes {
//		JAVA_PARSER, //Used to be based on githb java parser. If need something back that can extract docs from source code, we have to implement
						// based on JDT parser. But at the moment this wasn't being used so just got removed.
		HTML
	}

	private Supplier<JandexIndex> javaIndex;
	private final IClasspath classpath;
	private final FileObserver fileObserver;

	public JandexClasspath(IClasspath classpath, FileObserver fileObserver) {
		this.fileObserver = fileObserver;
		this.classpath = classpath;
		this.javaIndex = Suppliers.synchronizedSupplier(Suppliers.memoize(() -> createIndex()));
	}

	protected JandexIndex createIndex() {
		attachFolderListeners();
		Collection<File> classpathEntries = ImmutableList.of();
		try {
			classpathEntries = IClasspathUtil.getBinaryRoots(classpath);
		} catch (Exception e) {
			Log.log(e);
		}
		return new JandexIndex(classpathEntries, jarFile -> findIndexFile(jarFile), classpathResource -> {
			switch (providerType) {
//			case JAVA_PARSER:
//				return createParserJavadocProvider(classpathResource);
			case HTML:
				return createHtmlJavdocProvider(classpathResource);
			default:
				throw new IllegalStateException("Missing switch case?");
			}
		}, getBaseIndices());
	}

	private Disposable.Composite subscriptions = Disposables.composite();

	private void attachFolderListeners() {
		synchronized (this) {
			subscriptions.dispose();
			subscriptions = Disposables.composite();
		}
		for (File cpe : IClasspathUtil.getBinaryRoots(classpath)) {
			if (!cpe.toString().endsWith(".jar")) {
				final List<String> rebuildGlobPattern = Arrays.asList(cpe.toString().replace(File.separator, "/") + "/**/*.class");
				Disposable disposable = fileObserver.onAnyChange(rebuildGlobPattern, (uri) -> reindex());
				subscriptions.add(disposable);
			}
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

	private IJavadocProvider createHtmlJavdocProvider(File binaryClasspathRoot) {
		CPE cpe = IClasspathUtil.findEntryForBinaryRoot(classpath, binaryClasspathRoot);
		return JavaDocProviders.createFor(cpe);
	}

	@Override
	public Optional<URL> sourceContainer(File binaryClasspathRoot) {
		CPE cpe = IClasspathUtil.findEntryForBinaryRoot(classpath, binaryClasspathRoot);
		return cpe == null ? Optional.empty() : Optional.ofNullable(cpe.getSourceContainerUrl());
	}

	protected JandexIndex[] getBaseIndices() {
		return new JandexIndex[0];
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
	public Optional<File> findClasspathResourceContainer(String fqName) {
		return javaIndex.get().findClasspathResourceForType(fqName);
	}

	private void reindex() {
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

}
