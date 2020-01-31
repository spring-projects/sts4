/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJava.SCAN_PASS;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringIndexerJavaContext {

	private final IJavaProject project;
	private final CompilationUnit cu;
	private final String docURI;
	private final String file;
	private final long lastModified;
	private final AtomicReference<TextDocument> docRef;
	private final String content;
	private final List<CachedSymbol> generatedSymbols;
	private final SCAN_PASS pass;
	private final List<String> nextPassFiles;
	private final Set<String> dependencies = new HashSet<>();
	private final Set<String> scannedTypes;

	public SpringIndexerJavaContext(
			IJavaProject project, 
			CompilationUnit cu, 
			String docURI, 
			String file, 
			long lastModified,
			AtomicReference<TextDocument> docRef, 
			String content, 
			List<CachedSymbol> generatedSymbols, 
			SCAN_PASS pass,
			List<String> nextPassFiles, 
			Set<String> scannedTypes
	) {
		super();
		this.project = project;
		this.cu = cu;
		this.docURI = docURI;
		this.file = file;
		this.lastModified = lastModified;
		this.docRef = docRef;
		this.content = content;
		this.generatedSymbols = generatedSymbols;
		this.pass = pass;
		this.nextPassFiles = nextPassFiles;
		this.scannedTypes = scannedTypes;
	}

	public IJavaProject getProject() {
		return project;
	}

	public CompilationUnit getCu() {
		return cu;
	}

	public String getDocURI() {
		return docURI;
	}

	public String getFile() {
		return file;
	}

	public long getLastModified() {
		return lastModified;
	}

	public AtomicReference<TextDocument> getDocRef() {
		return docRef;
	}

	public String getContent() {
		return content;
	}

	public List<CachedSymbol> getGeneratedSymbols() {
		return generatedSymbols;
	}

	public SCAN_PASS getPass() {
		return pass;
	}

	public List<String> getNextPassFiles() {
		return nextPassFiles;
	}

	public Set<String> getDependencies() {
		return dependencies;
	}
	
	public void addDependency(ITypeBinding dependsOn) {
		dependencies.add(dependsOn.getKey());
	}

	public Set<String> getScannedTypes() {
		return scannedTypes;
	}
	
	public void addScannedType(ITypeBinding scannedType) {
		if (this.scannedTypes != null && scannedType != null) {
			scannedTypes.add(scannedType.getKey());
		}
	}
}
