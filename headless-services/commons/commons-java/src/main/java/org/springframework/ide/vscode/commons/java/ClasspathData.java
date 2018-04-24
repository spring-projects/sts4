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
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Objects;

public class ClasspathData {

	final public static ClasspathData EMPTY_CLASSPATH_DATA = new ClasspathData(null, Collections.emptySet(),
			Collections.emptySet(), null);
	
	private String name;
	private Set<CPE> classpathEntries;
	private Set<String> classpathResources;
	private String outputFolder;

	public ClasspathData() {
	}

	public ClasspathData(String name, Set<CPE> classpathEntries, Set<String> classpathResources, String outputFolder) {
		this.name = name;
		this.classpathEntries = classpathEntries;
		this.classpathResources = classpathResources;
		this.outputFolder = outputFolder;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<CPE> getClasspathEntries() {
		return classpathEntries;
	}

	public void setClasspathEntries(Set<CPE> classpathEntries) {
		this.classpathEntries = classpathEntries;
	}

	public Set<String> getClasspathResources() {
		return classpathResources;
	}

	public void setClasspathResources(Set<String> classpathResources) {
		this.classpathResources = classpathResources;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public static ClasspathData getEmptyClasspathData() {
		return EMPTY_CLASSPATH_DATA;
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
	
	public static ClasspathData from(String name, Collection<CPE> classpathEntries,
			Collection<String> classpathResources, Path outputFolder) {
		return new ClasspathData(name, 
				new LinkedHashSet<>(classpathEntries), 
				new LinkedHashSet<>(classpathResources),
				outputFolder==null ? null : outputFolder.toString()
		);
	}
}