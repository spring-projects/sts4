/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Objects;

public class ClasspathData {

	final public static ClasspathData EMPTY_CLASSPATH_DATA = new ClasspathData(null, Collections.emptySet(),
			Collections.emptySet(), null);
	
	final public String name;
	final public Set<Path> classpathEntries;
	final public Set<String> classpathResources;
	final public Path outputFolder;

	public ClasspathData(String name, Set<Path> classpathEntries, Set<String> classpathResources, Path outputFolder) {
		this.name = name;
		this.classpathEntries = classpathEntries;
		this.classpathResources = classpathResources;
		this.outputFolder = outputFolder;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClasspathData) {
			ClasspathData other = (ClasspathData) obj;
			try {
				return Objects.equal(name, other.name) && Objects.equal(classpathEntries, other.classpathEntries)
						&& Objects.equal(classpathResources, other.classpathResources)
						&& Objects.equal(outputFolder, outputFolder);
			} catch (Throwable t) {
				Log.log(t);
			}
		}
		return false;
	}
	
	public static ClasspathData from(String name, Collection<Path> classpathEntries,
			Collection<String> classpathResources, Path outputFolder) {
		return new ClasspathData(name, new LinkedHashSet<>(classpathEntries), new LinkedHashSet<>(classpathResources),
				outputFolder);
	}
}