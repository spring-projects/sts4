/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

public class SpringIndexerJavaDependencyTracker {
	
	private static final Logger log = LoggerFactory.getLogger(SpringIndexerJavaDependencyTracker.class);

	private Multimap<String, String> dependencies = MultimapBuilder.hashKeys().hashSetValues().build();
	
	public void addDependency(String sourceFile, ITypeBinding dependsOn) {
		dependencies.put(sourceFile, dependsOn.getKey());
	}
	
	public void dump() {
		log.info("=== Dependencies ===");
		for (String sourceFile : dependencies.keySet()) {
			Collection<String> values = dependencies.get(sourceFile);
			if (!values.isEmpty())
			log.info(sourceFile + "=> ");
			for (String v : values) {
				log.info("   "+v);
			}
		}
		log.info("======================");
	}

	public Multimap<String, String> getAllDependencies() {
		return dependencies;
	}

	public void update(String file, Set<String> dependenciesForFile) {
		dependencies.replaceValues(file, dependenciesForFile);
	}

	public void restore(Multimap<String, String> deps) {
		this.dependencies = deps;
	}
}