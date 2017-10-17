/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * Composite project manager that acts a single project manager but consissts of many project managers
 * 
 * @author Alex Boyko
 *
 */
public class CompositeJavaProjectFinder extends AbstractJavaProjectFinder {
	
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
	public IJavaProject find(File file) {
		return projectFinders.stream().map(finder -> finder.find(file)).filter(Objects::nonNull).findFirst().orElse(null);
	}
}
