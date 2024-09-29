/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.annotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class AnnotationAttributeCompletionProcessor implements CompletionProvider {
	
	private final JavaProjectFinder projectFinder;
	private final Map<String, AnnotationAttributeCompletionProvider> completionProviders;

	public AnnotationAttributeCompletionProcessor(JavaProjectFinder projectFinder, Map<String, AnnotationAttributeCompletionProvider> completionProviders) {
		this.projectFinder = projectFinder;
		this.completionProviders = completionProviders;
	}

	@Override
	public void provideCompletions(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc, Collection<ICompletionProposal> completions) {

		Optional<IJavaProject> optionalProject = projectFinder.find(doc.getId());
		if (!optionalProject.isPresent()) {
			return;
		}
		
		IJavaProject project = optionalProject.get();
		
		try {
			
			// in case the node is embedded in an qualified name, e.g. "file.txt", use the fully qualified node instead just a part
			if (node instanceof Name && node.getParent() instanceof QualifiedName) {
				node = node.getParent();
			}
			
			// case: @Qualifier(<*>)
			if (node == annotation && doc.get(offset - 1, 2).endsWith("()")) {
				createCompletionProposals(project, doc, node, "value", completions, offset, offset, "", (beanName) -> "\"" + beanName + "\"");
			}
			// case: @Qualifier(prefix<*>)
			else if (node instanceof Name && node.getParent() instanceof Annotation
					&& node != annotation.getTypeName()) {
				computeProposalsForSimpleName(project, node, "value", completions, offset, doc);
			}
			// case: @Qualifier(value=<*>)
			else if (node instanceof Name && node.getParent() instanceof MemberValuePair
					&& completionProviders.containsKey(((MemberValuePair)node.getParent()).getName().toString())) {
				String attributeName = ((MemberValuePair)node.getParent()).getName().toString();
				computeProposalsForSimpleName(project, node, attributeName, completions, offset, doc);
			}
			// case: @Qualifier("prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForStringLiteral(project, node, "value", completions, offset, doc);
				}
			}
			// case: @Qualifier({"prefix<*>"})
			else if (node instanceof StringLiteral && node.getParent() instanceof ArrayInitializer) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForInsideArrayInitializer(project, node, "value", completions, offset, doc);
				}
			}
			// case: @Qualifier(value="prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
					&& completionProviders.containsKey(((MemberValuePair)node.getParent()).getName().toString())) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					String attributeName = ((MemberValuePair)node.getParent()).getName().toString();
					computeProposalsForStringLiteral(project, node, attributeName, completions, offset, doc);
				}
			}
			// case: @Qualifier({<*>})
			else if (node instanceof ArrayInitializer && node.getParent() instanceof Annotation) {
				computeProposalsForArrayInitializr(project, (ArrayInitializer) node, "value", completions, offset, doc);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * create the concrete completion proposal
	 */
	private void createCompletionProposals(IJavaProject project, TextDocument doc, ASTNode node, String attributeName,
			Collection<ICompletionProposal> completions, int startOffset, int endOffset, String filterPrefix,
			Function<String, String> createReplacementText) {

		Set<String> alreadyMentionedValues = alreadyMentionedValues(node);

		AnnotationAttributeCompletionProvider completionProvider = this.completionProviders.get(attributeName);
		if (completionProvider != null) {

			Map<String, String> proposals = completionProvider.getCompletionCandidates(project);
			Map<String, String> filteredProposals = proposals.entrySet().stream()
					.filter(candidate -> candidate.getKey().toLowerCase().contains(filterPrefix.toLowerCase()))
					.filter(candidate -> !alreadyMentionedValues.contains(candidate.getKey()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (u, v) -> u, LinkedHashMap::new));
			double score = filteredProposals.size();
			for (Map.Entry<String, String> entry : filteredProposals.entrySet()) {
				String candidate = entry.getKey();
				DocumentEdits edits = new DocumentEdits(doc, false);
				edits.replace(startOffset, endOffset, createReplacementText.apply(candidate));
				
				AnnotationAttributeCompletionProposal proposal = new AnnotationAttributeCompletionProposal(edits,
						candidate, entry.getValue(), null, score--);
				completions.add(proposal);

			}
		}
	}

	//
	// internal computation of the right positions, prefixes, etc.
	//
	
	private void computeProposalsForSimpleName(IJavaProject project, ASTNode node, String attributeName, Collection<ICompletionProposal> completions, int offset, TextDocument doc) {
		String prefix = identifyPropertyPrefix(node.toString(), offset - node.getStartPosition());

		int startOffset = node.getStartPosition();

		// special adjustment for the case of a $missing$ SimpleName node, e.g. @DependsOn(value = <*>)
		// where the node starts right after the "=", not at the offset
		if (node instanceof SimpleName && "$missing$".equals(((SimpleName)node).getIdentifier())) {
			startOffset = offset;
			prefix = "";
		}
		
		int endOffset = startOffset + node.getLength();

		String proposalPrefix = "\"";
		String proposalPostfix = "\"";

		createCompletionProposals(project, doc, node, attributeName, completions, startOffset, endOffset, prefix, (beanName) -> proposalPrefix + beanName + proposalPostfix);
	}

	private void computeProposalsForStringLiteral(IJavaProject project, ASTNode node, String attributeName, Collection<ICompletionProposal> completions, int offset, TextDocument doc) throws BadLocationException {
		int length = offset - (node.getStartPosition() + 1);

		String prefix = identifyPropertyPrefix(doc.get(node.getStartPosition() + 1, length), length);
		int startOffset = offset - prefix.length();
		int endOffset = node.getStartPosition() + node.getLength() - 1;

		createCompletionProposals(project, doc, node, attributeName, completions, startOffset, endOffset, prefix, (beanName) -> beanName);
	}
	
	private void computeProposalsForArrayInitializr(IJavaProject project, ArrayInitializer node, String attributeName, Collection<ICompletionProposal> completions, int offset, TextDocument doc) {
		createCompletionProposals(project, doc, node, attributeName, completions, offset, offset, "", (beanName) -> "\"" + beanName + "\"");
	}
	
	private void computeProposalsForInsideArrayInitializer(IJavaProject project, ASTNode node, String attributeName, Collection<ICompletionProposal> completions, int offset, TextDocument doc) throws BadLocationException {
		int length = offset - (node.getStartPosition() + 1);
		if (length >= 0) {
			computeProposalsForStringLiteral(project, node, attributeName, completions, offset, doc);
		}
		else {
			createCompletionProposals(project, doc, node, attributeName, completions, offset, offset, "", (beanName) -> "\"" + beanName + "\",");
		}
	}

	private String identifyPropertyPrefix(String nodeContent, int offset) {
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
	
	protected Set<String> alreadyMentionedValues(ASTNode node) {
		Set<String> result = new HashSet<>();
		
		ArrayInitializer arrayNode = null;
		while (node != null && arrayNode == null && !(node instanceof Annotation)) {
			if (node instanceof ArrayInitializer) {
				arrayNode = (ArrayInitializer) node;
			}
			else {
				node = node.getParent();
			}
		}
		
		if (arrayNode != null) {
			List<?> expressions = arrayNode.expressions();
			for (Object expression : expressions) {
				if (expression instanceof StringLiteral) {
					StringLiteral stringExr = (StringLiteral) expression;
					String value = stringExr.getLiteralValue();
					result.add(value);
				}
			}
		}
		
		return result;
	}
	
}
