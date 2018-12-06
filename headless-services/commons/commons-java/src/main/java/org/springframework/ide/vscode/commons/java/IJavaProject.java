/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public interface IJavaProject {

	final static String PROJECT_CACHE_FOLDER = ".sts4-cache";

	IClasspath getClasspath();
	ClasspathIndex getIndex();
	URI getLocationUri();
	boolean exists();

	default String getElementName() {
		return getClasspath().getName();
	}

	default IType findType(String fqName) {
		return getIndex().findType(fqName);
	}

	default Flux<IType> allSubtypesOf(IType targetType) {
		return getIndex().allSubtypesOf(targetType);
	}

	default Flux<IType> allSuperTypesOf(IType targetType) {
		return getIndex().allSuperTypesOf(targetType);
	}

	default Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, Predicate<IType> typeFilter) {
		return getIndex().fuzzySearchTypes(searchTerm, typeFilter);
	}

	default Optional<URL> sourceContainer(File classpathResource) {
		return getIndex().sourceContainer(classpathResource);
	}

	default List<String> getClasspathResources() {
		return getIndex().getClasspathResources();
	}

	default IJavaModuleData findClasspathResourceContainer(String fqName) {
		return getIndex().findClasspathResourceContainer(fqName);
	}

}
