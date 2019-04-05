/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml;

import java.util.Collection;
import java.util.Collections;

import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringXMLCompletionEngine implements ICompletionEngine {

	public SpringXMLCompletionEngine(SpringXMLLanguageServerComponents springXMLLanguageServerComponents) {
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(TextDocument document, int offset) throws Exception {
		System.out.println(" SUPER COOL SPRING XML COMPLETION PROPOSALS COMING SOON... STAY TUNED...");

		return Collections.emptyList();
	}

}
