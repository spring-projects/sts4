/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.properties.completions;

import java.util.Collection;

import org.springframework.ide.vscode.boot.common.PropertyCompletionFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;;

/**
 * @author Kris De Volder
 */
public class SpringPropertiesCompletionEngine implements ICompletionEngine {

	private boolean preferLowerCaseEnums = true; //might make sense to make this user configurable

	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;
	private PropertyCompletionFactory completionFactory = null;
	private SourceLinks sourceLinks;

	/**
	 * Constructor used in 'production'. Wires up stuff properly for running inside a normal
	 * Eclipse runtime.
	 */
	public SpringPropertiesCompletionEngine(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider, JavaProjectFinder projectFinder, SourceLinks sourceLinks) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		this.completionFactory = new PropertyCompletionFactory();
		this.sourceLinks = sourceLinks;
	}

	/**
	 * Create completions proposals in the context of a properties text editor.
	 */
	@Override
	public Collection<ICompletionProposal> getCompletions(TextDocument doc, int offset) throws BadLocationException {
		return new PropertiesCompletionProposalsCalculator(indexProvider.getIndex(doc).getProperties(),
				typeUtilProvider.getTypeUtil(sourceLinks, doc), completionFactory, doc, offset, preferLowerCaseEnums).calculate();
	}

	public boolean getPreferLowerCaseEnums() {
		return preferLowerCaseEnums;
	}

	public void setPreferLowerCaseEnums(boolean preferLowerCaseEnums) {
		this.preferLowerCaseEnums = preferLowerCaseEnums;
	}

}