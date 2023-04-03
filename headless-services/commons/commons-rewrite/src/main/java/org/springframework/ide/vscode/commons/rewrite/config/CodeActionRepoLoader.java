/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.config;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.openrewrite.internal.RecipeIntrospectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class CodeActionRepoLoader {

	final static Logger log = LoggerFactory.getLogger(CodeActionRepoLoader.class);

	private final List<CodeActionRepository> codeActionRepos = new ArrayList<>();
	
    public CodeActionRepoLoader(String... acceptPackages) {
        scanClasses(new ClassGraph().acceptPackages(acceptPackages), getClass().getClassLoader());
    }

	public CodeActionRepoLoader(Path p, ClassLoader classLoader) {
		if (Files.isDirectory(p)) {
			String dir = p.toString();

			scanClasses(new ClassGraph().acceptPaths(dir).ignoreParentClassLoaders().overrideClassLoaders(classLoader),
					classLoader);

		} else {
			String jarName = p.toFile().getName();

			scanClasses(
					new ClassGraph().acceptJars(jarName).ignoreParentClassLoaders().overrideClassLoaders(classLoader),
					classLoader);

		}
	}

	private void scanClasses(ClassGraph classGraph, ClassLoader classLoader) {
		try (ScanResult result = classGraph.ignoreClassVisibility().overrideClassLoaders(classLoader).scan()) {

			for (ClassInfo classInfo : result.getSubclasses(CodeActionRepository.class.getName())) {
				Class<?> codeActionRepoClass = classInfo.loadClass();
				Constructor<?> primaryConstructor = RecipeIntrospectionUtils
						.getZeroArgsConstructor(codeActionRepoClass);
				if (primaryConstructor == null) {
					// TODO: error!!!
				} else {
					try {
						CodeActionRepository repo = (CodeActionRepository) primaryConstructor.newInstance();
						codeActionRepos.add(repo);
					} catch (Throwable t) {
						log.warn("Unable to configure " + codeActionRepoClass.getName(), t);
					}
				}

			}
		}
	}

	public List<CodeActionRepository> listCodeActionDescriptorsRepositories() {
		return codeActionRepos;
	}

}
