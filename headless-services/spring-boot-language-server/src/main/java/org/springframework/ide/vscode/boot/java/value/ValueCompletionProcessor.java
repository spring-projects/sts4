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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
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
	private SpringPropertyIndexProvider adHocIndexProvider;

	public ValueCompletionProcessor(SpringPropertyIndexProvider indexProvider, SpringPropertyIndexProvider adHocIndexProvider) {
		this.indexProvider = indexProvider;
		this.adHocIndexProvider = adHocIndexProvider;
	}

	@Override
	public Collection<ICompletionProposal> provideCompletions(ASTNode node, Annotation annotation, ITypeBinding type,
			int offset, IDocument doc) {

		List<ICompletionProposal> result = new ArrayList<>();

		try {
			// case: @Value(<*>)
			if (node == annotation && doc.get(offset - 1, 2).endsWith("()")) {
				List<Match<PropertyInfo>> matches = findMatches("", doc);

				for (Match<PropertyInfo> match : matches) {

					DocumentEdits edits = new DocumentEdits(doc);
					edits.replace(offset, offset, "\"${" + match.data.getId() + "}\"");

					ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match);
					result.add(proposal);
				}
			}
			// case: @Value(prefix<*>)
			else if (node instanceof SimpleName && node.getParent() instanceof Annotation) {
				computeProposalsForSimpleName(node, result, offset, doc);
			}
			// case: @Value(value=<*>)
			else if (node instanceof SimpleName && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				computeProposalsForSimpleName(node, result, offset, doc);
			}
			// case: @Value("prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForStringLiteral(node, result, offset, doc);
				}
			}
			// case: @Value(value="prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForStringLiteral(node, result, offset, doc);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private void computeProposalsForSimpleName(ASTNode node, List<ICompletionProposal> completions, int offset,
			IDocument doc) {
		String prefix = identifyPropertyPrefix(node.toString(), offset - node.getStartPosition());

		int startOffset = node.getStartPosition();
		int endOffset = node.getStartPosition() + node.getLength();

		String proposalPrefix = "\"";
		String proposalPostfix = "\"";

		List<Match<PropertyInfo>> matches = findMatches(prefix, doc);

		for (Match<PropertyInfo> match : matches) {

			DocumentEdits edits = new DocumentEdits(doc);
			edits.replace(startOffset, endOffset, proposalPrefix + "${" + match.data.getId() + "}" + proposalPostfix);

			ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match);
			completions.add(proposal);
		}
	}

	private void computeProposalsForStringLiteral(ASTNode node, List<ICompletionProposal> completions, int offset,
			IDocument doc) throws BadLocationException {
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

		List<Match<PropertyInfo>> matches = findMatches(prefix, doc);

		for (Match<PropertyInfo> match : matches) {

			DocumentEdits edits = new DocumentEdits(doc);
			edits.replace(startOffset, endOffset, preCompletion + match.data.getId() + postCompletion);

			ValuePropertyKeyProposal proposal = new ValuePropertyKeyProposal(edits, match);
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

	private List<Match<PropertyInfo>> findMatches(String prefix, IDocument doc) {
		FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
		List<Match<PropertyInfo>> matches =index.find(camelCaseToHyphens(prefix));

		//First the 'real' properties.
		Set<String> suggestedKeys = new HashSet<>();
		for (Match<PropertyInfo> m : matches) {
			suggestedKeys.add(m.data.getId());
		}

		//Then also add 'ad-hoc' properties (see https://www.pivotaltracker.com/story/show/153107266).
		index = adHocIndexProvider.getIndex(doc);
		for (Match<PropertyInfo> m : index.find(prefix)) {
			if (suggestedKeys.add(m.data.getId())) {
				matches.add(m);
			}
		}
		return matches;
	}

}
