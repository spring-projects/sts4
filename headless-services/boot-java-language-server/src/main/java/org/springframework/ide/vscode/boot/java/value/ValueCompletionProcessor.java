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
package org.springframework.ide.vscode.boot.java.value;

import static org.springframework.ide.vscode.commons.util.StringUtil.camelCaseToHyphens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.java.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.FuzzyMap.Match;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class ValueCompletionProcessor implements CompletionProvider {

	private final SpringPropertyIndexProvider indexProvider;

	public ValueCompletionProcessor(SpringPropertyIndexProvider indexProvider) {
		this.indexProvider = indexProvider;
	}

	@Override
	public Collection<ICompletionProposal> provideCompletions(ASTNode node, Annotation annotation, ITypeBinding type,
			int offset, IDocument doc) {

		List<ICompletionProposal> result = new ArrayList<>();

		try {
			FuzzyMap<ConfigurationMetadataProperty> index = indexProvider.getIndex(doc);

			// case: @Value(<*>)
			if (node == annotation && doc.get(offset - 1, 2).endsWith("()")) {
				List<Match<ConfigurationMetadataProperty>> matches = findMatches("", index);

				for (Match<ConfigurationMetadataProperty> match : matches) {

					DocumentEdits edits = new DocumentEdits(doc);
					edits.replace(offset, offset, "\"${" + match.data.getId() + "}\"");

					ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match.data.getId(), match.data.getName(), null);
					result.add(proposal);
				}
			}
			// case: @Value(prefix<*>)
			else if (node instanceof SimpleName && node.getParent() instanceof Annotation) {
				computeProposalsForSimpleName(node, result, offset, doc, index);
			}
			// case: @Value(value=<*>)
			else if (node instanceof SimpleName && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				computeProposalsForSimpleName(node, result, offset, doc, index);
			}
			// case: @Value("prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForStringLiteral(node, result, offset, doc, index);
				}
			}
			// case: @Value(value="prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForStringLiteral(node, result, offset, doc, index);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private void computeProposalsForSimpleName(ASTNode node, List<ICompletionProposal> completions, int offset,
			IDocument doc, FuzzyMap<ConfigurationMetadataProperty> index) {
		String prefix = identifyPropertyPrefix(node.toString(), offset - node.getStartPosition());

		int startOffset = node.getStartPosition();
		int endOffset = node.getStartPosition() + node.getLength();

		String proposalPrefix = "\"";
		String proposalPostfix = "\"";

		List<Match<ConfigurationMetadataProperty>> matches = findMatches(prefix, index);

		for (Match<ConfigurationMetadataProperty> match : matches) {

			DocumentEdits edits = new DocumentEdits(doc);
			edits.replace(startOffset, endOffset, proposalPrefix + "${" + match.data.getId() + "}" + proposalPostfix);

			ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match.data.getId(), match.data.getName(), null);
			completions.add(proposal);
		}
	}

	private void computeProposalsForStringLiteral(ASTNode node, List<ICompletionProposal> completions, int offset,
			IDocument doc, FuzzyMap<ConfigurationMetadataProperty> index) throws BadLocationException {
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

		List<Match<ConfigurationMetadataProperty>> matches = findMatches(prefix, index);

		for (Match<ConfigurationMetadataProperty> match : matches) {

			DocumentEdits edits = new DocumentEdits(doc);
			edits.replace(startOffset, endOffset, preCompletion + match.data.getId() + postCompletion);

			ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match.data.getId(), match.data.getName(), null);
			completions.add(proposal);
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

	private List<Match<ConfigurationMetadataProperty>> findMatches(String prefix, FuzzyMap<ConfigurationMetadataProperty> index) {
		List<Match<ConfigurationMetadataProperty>> matches = index.find(camelCaseToHyphens(prefix));
		return matches;
	}

}
