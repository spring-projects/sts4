/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.commons.java.IJavaProject;

import com.google.common.collect.ImmutableList;

/**
 * Composite project manager that acts a single project manager but consists of many project managers
 *
 * @author Alex Boyko
 *
 */
public class CompositeJavaProjectFinder implements JavaProjectFinder {

	private final List<JavaProjectFinder> projectFinders;

	public CompositeJavaProjectFinder(Collection<JavaProjectFinder> projectFinders) {
		this.projectFinders = new ArrayList<>(projectFinders);
	}

	public CompositeJavaProjectFinder() {
		this(Collections.emptyList());
	}

	public boolean addJavaProjectFinder(JavaProjectFinder javaProjectFinder) {
		return projectFinders.add(javaProjectFinder);
	}

	public boolean removeJavaProjectFinder(JavaProjectFinder javaProjectFinder) {
		return projectFinders.remove(javaProjectFinder);
	}

	@Override
	public Optional<IJavaProject> find(TextDocumentIdentifier doc) {
		for (JavaProjectFinder finder : projectFinders) {
			Optional<IJavaProject> project = finder.find(doc);
			if (project.isPresent()) {
				return project;
			}
		}
		return Optional.empty();
	}

	@Override
	public Collection<? extends IJavaProject> all() {
		ImmutableList.Builder<IJavaProject> builder = ImmutableList.builder();
		for (JavaProjectFinder projectFinder : projectFinders) {
			builder.addAll(projectFinder.all());
		}
		return builder.build();
	}

}
