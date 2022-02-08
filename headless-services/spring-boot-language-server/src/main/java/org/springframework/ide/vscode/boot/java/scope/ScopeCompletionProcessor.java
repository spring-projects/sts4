/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.scope;

import java.util.Collection;

import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.Assignment;
import org.openrewrite.java.tree.J.Empty;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class ScopeCompletionProcessor implements CompletionProvider {

	@Override
	public void provideCompletions(J node, Annotation annotation,
			int offset, IDocument doc, Collection<ICompletionProposal> completions) {

		try {
			if (node instanceof Assignment) {
				Assignment assignment = (Assignment) node;

				// case: @Scope(value=<*>)
				if ("value".equals(assignment.getVariable().toString()) && assignment.getAssignment() == null) {
					for (ScopeNameCompletion completion : ScopeNameCompletionProposal.COMPLETIONS) {
						ICompletionProposal proposal = new ScopeNameCompletionProposal(completion, doc, offset, offset, "");
						completions.add(proposal);
					}
				}
			}
			// case: @Scope(<*>)
			else if (node instanceof Empty && ORAstUtils.getParent(node) == annotation) {
				for (ScopeNameCompletion completion : ScopeNameCompletionProposal.COMPLETIONS) {
					ICompletionProposal proposal = new ScopeNameCompletionProposal(completion, doc, offset, offset, "");
					completions.add(proposal);
				}
			}
			else if (node instanceof Literal && ORAstUtils.getParent(node) == annotation) {
				String nodeStr = node.printTrimmed();
				if (nodeStr.startsWith("\"") && nodeStr.endsWith("\"")) {
					// case: @Scope("...")
					Range range = node.getMarkers().findFirst(Range.class).orElseThrow();
					String prefix = doc.get(range.getStart().getOffset(), offset - range.getStart().getOffset());
					for (ScopeNameCompletion completion : ScopeNameCompletionProposal.COMPLETIONS) {
						if (completion.getValue().startsWith(prefix)) {
							ICompletionProposal proposal = new ScopeNameCompletionProposal(completion, doc, range.getStart().getOffset(), range.getStart().getOffset() + range.length(), prefix);
							completions.add(proposal);
						}
					}

				}
			}
			// case: @Scope(value=<*>)
			else if (node instanceof Literal && ORAstUtils.getParent(node) instanceof Assignment) {
				Assignment assignment = (Assignment) ORAstUtils.getParent(node);
				String nodeStr = node.printTrimmed();
				
				if ("value".equals(assignment.getVariable().printTrimmed()) && nodeStr.startsWith("\"") && nodeStr.endsWith("\"")) {
					Range range = node.getMarkers().findFirst(Range.class).orElseThrow();
					String prefix = doc.get(range.getStart().getOffset(), offset - range.getStart().getOffset());
					for (ScopeNameCompletion completion : ScopeNameCompletionProposal.COMPLETIONS) {
						if (completion.getValue().startsWith(prefix)) {
							ICompletionProposal proposal = new ScopeNameCompletionProposal(completion, doc, range.getStart().getOffset(), range.getStart().getOffset() + range.length(), prefix);
							completions.add(proposal);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void provideCompletions(J node, int offset, IDocument doc, Collection<ICompletionProposal> completions) {
	}

}
