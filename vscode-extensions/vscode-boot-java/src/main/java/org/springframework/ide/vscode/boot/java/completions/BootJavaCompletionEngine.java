/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.java.completions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class BootJavaCompletionEngine implements ICompletionEngine {

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument document, int offset) throws Exception {
		List<ICompletionProposal> completions = new ArrayList<>();
		
		ICompletionProposal proposal = new ICompletionProposal() {
			
			@Override
			public DocumentEdits getTextEdit() {
				DocumentEdits edits = new DocumentEdits(document);
				edits.insert(0, "my noew java code from spring boot completions!!!");
				
				return edits;
			}
			
			@Override
			public String getLabel() {
				return "Do a great Spring Boot Java Code completion";
			}
			
			@Override
			public CompletionItemKind getKind() {
				return CompletionItemKind.Text;
			}
			
			@Override
			public Renderable getDocumentation() {
				return Renderables.text("this describes the Boot Java completion in more detail");
			}
			
			@Override
			public String getDetail() {
				return "and here are the details";
			}
			
			@Override
			public ICompletionProposal deemphasize() {
				return null;
			}
		};
		
		completions.add(proposal);
		
		return completions;
	}

}
