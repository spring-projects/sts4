/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.completions;

import static org.springframework.ide.vscode.commons.util.StringUtil.camelCaseToHyphens;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.boot.metadata.util.FuzzyMap.Match;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class ValueCompletionProcessor {

	private FuzzyMap<PropertyInfo> index;
	
	public ValueCompletionProcessor(FuzzyMap<PropertyInfo> index) {
		this.index = index;
	}

	public void collectCompletionsForValueAnnotation(ASTNode node, Annotation annotation, ITypeBinding type,
			List<ICompletionProposal> completions, int offset, IDocument doc) {
		
		try {
			// case: @Value(<*>)
			if (node == annotation && doc.get(offset - 1, 2).endsWith("()")) {
				List<Match<PropertyInfo>> matches = findMatches("");
				
				for (Match<PropertyInfo> match : matches) {

					DocumentEdits edits = new DocumentEdits(doc);
					edits.replace(offset, offset, "\"${" + match.data.getId() + "}\"");
	
					ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match.data.getId(), match.data.getName(), null);
					completions.add(proposal);
				}
			}
			// case: @Value(prefix<*>)
			else if (node instanceof SimpleName && node.getParent() instanceof Annotation) {
				String prefix = identifyPropertyPrefix(node.toString(), offset - node.getStartPosition());
				
				int startOffset = node.getStartPosition();
				int endOffset = node.getStartPosition() + node.getLength();

				String proposalPrefix = "\"";
				String proposalPostfix = "\"";

				List<Match<PropertyInfo>> matches = findMatches(prefix);
				
				for (Match<PropertyInfo> match : matches) {

					DocumentEdits edits = new DocumentEdits(doc);
					edits.replace(startOffset, endOffset, proposalPrefix + "${" + match.data.getId() + "}" + proposalPostfix);
	
					ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match.data.getId(), match.data.getName(), null);
					completions.add(proposal);
				}
			}
			// case: @Value("prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {

					String prefix = identifyPropertyPrefix(doc.get(node.getStartPosition() + 1, offset - (node.getStartPosition() + 1)), offset - (node.getStartPosition() + 1));
					
					int startOffset = offset - prefix.length();
					int endOffset = offset;
					
					
					String prePrefix = doc.get(node.getStartPosition() + 1, offset - prefix.length() - node.getStartPosition() - 1);

					String preCompletion;
					if (prePrefix.endsWith("${")) {
						preCompletion = "";
					}
					else if (prePrefix.endsWith("$")) {
						preCompletion = "{";
					}
					else {
						preCompletion = "${";
					}
					
					String fullNodeContent = doc.get(node.getStartPosition(), node.getLength());
					String postCompletion = isClosingBracketMissing(fullNodeContent + preCompletion) ? "}" : "";

					List<Match<PropertyInfo>> matches = findMatches(prefix);
					
					for (Match<PropertyInfo> match : matches) {

						DocumentEdits edits = new DocumentEdits(doc);
						edits.replace(startOffset, endOffset, preCompletion + match.data.getId() + postCompletion);
		
						ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match.data.getId(), match.data.getName(), null);
						completions.add(proposal);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean isClosingBracketMissing(String fullNodeContent) {
		int bracketOpens = 0;
		
		for (int i = 0; i < fullNodeContent.length(); i++) {
			if (fullNodeContent.charAt(i) == '{') {
				bracketOpens++;
			}
			else if (fullNodeContent.charAt(i) == '}') {
				bracketOpens--;
			}
		}
		
		return bracketOpens > 0;
	}

	public String identifyPropertyPrefix(String nodeContent, int offset) {
		String result = nodeContent.substring(0, offset);
		
		int i = offset - 1;
		while (i >= 0) {
			char c = nodeContent.charAt(i);
			if (c == '}' || c == '{'  || c == '$' || c == '#') {
				result = result.substring(i + 1, offset);
				break;
			}
			i--;
		}
		
		return result;
	}

	private List<Match<PropertyInfo>> findMatches(String prefix) {
		List<Match<PropertyInfo>> matches = index.find(camelCaseToHyphens(prefix));
		return matches;
	}

}
