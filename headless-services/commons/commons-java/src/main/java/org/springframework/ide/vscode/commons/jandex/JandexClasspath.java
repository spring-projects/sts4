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
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * Classpath with Jandex Java index for searching types
 * 
 * @author Alex Boyko
 *
 */
public abstract class JandexClasspath implements IClasspath {
	
	public static JavadocProviderTypes providerType = JavadocProviderTypes.HTML;
	
	public enum JavadocProviderTypes {
		JAVA_PARSER,
		HTML
	}
	
	private Supplier<JandexIndex> javaIndex;
	
	public JandexClasspath() {
		this.javaIndex = Suppliers.memoize(() -> createIndex());
	}
	
	protected JandexIndex createIndex() {
		Stream<Path> classpathEntries = Stream.empty();
		try {
			classpathEntries = getClasspathEntries().stream();
		} catch (Exception e) {
			Log.log(e);
		}
		return new JandexIndex(classpathEntries.map(p -> p.toFile()).collect(Collectors.toList()), jarFile -> findIndexFile(jarFile), classpathResource -> {
			try {
				switch (providerType) {
				case JAVA_PARSER:
					return createParserJavadocProvider(classpathResource);
				default:
					return createHtmlJavdocProvider(classpathResource);
				}
			} catch (Exception e) {
				Log.log(e);
			}
			return null;
		}, getBaseIndices());
	}
	
	protected JandexIndex[] getBaseIndices() {
		return new JandexIndex[0];
	}
	
	public IType findType(String fqName) {
		return javaIndex.get().findType(fqName);
	}
	
	public Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, Predicate<IType> typeFilter) {
		return javaIndex.get().fuzzySearchTypes(searchTerm, typeFilter);
	}
	
	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm) {
		return javaIndex.get().fuzzySearchPackages(searchTerm);
	}

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

	abstract protected IJavadocProvider createParserJavadocProvider(File classpathResource) throws Exception;
	
	abstract protected IJavadocProvider createHtmlJavdocProvider(File classpathResource) throws Exception;
	
}
