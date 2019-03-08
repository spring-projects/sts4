/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.protocol.java.Classpath.CPE;

import com.google.common.collect.ImmutableSet;

public class ClasspathData implements IClasspath {

	private static final Logger log = LoggerFactory.getLogger(ClasspathData.class);

	final public static ClasspathData EMPTY_CLASSPATH_DATA = new ClasspathData(
			null,
			Collections.emptySet()
	);

	private String name;
	private Set<CPE> classpathEntries;

	public ClasspathData() {}

	public ClasspathData(String name, Collection<CPE> classpathEntries) {
		this.name = name;
		this.classpathEntries = ImmutableSet.copyOf(classpathEntries);
	}


	public static ClasspathData from(IClasspath d) {
		Collection<CPE> entries = null;
		try {
			entries = d.getClasspathEntries();
		} catch (Exception e) {
			log.error("", e);
		}
		return new ClasspathData(
				d.getName(),
				entries==null ? ImmutableSet.of() : entries
		);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Set<CPE> getClasspathEntries() {
		return classpathEntries;
	}

	public void setClasspathEntries(Set<CPE> classpathEntries) {
		this.classpathEntries = classpathEntries;
	}

	public static ClasspathData getEmptyClasspathData() {
		return EMPTY_CLASSPATH_DATA;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classpathEntries == null) ? 0 : classpathEntries.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClasspathData other = (ClasspathData) obj;
		if (classpathEntries == null) {
			if (other.classpathEntries != null)
				return false;
		} else if (!classpathEntries.equals(other.classpathEntries))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}