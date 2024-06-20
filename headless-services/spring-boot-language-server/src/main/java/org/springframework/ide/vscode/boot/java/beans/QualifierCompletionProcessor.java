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
package org.springframework.ide.vscode.boot.java.beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.handlers.CompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class QualifierCompletionProcessor implements CompletionProvider {

	private final JavaProjectFinder projectFinder;
	private final SpringMetamodelIndex springIndex;

	public QualifierCompletionProcessor(JavaProjectFinder projectFinder, SpringMetamodelIndex springIndex) {
		this.projectFinder = projectFinder;
		this.springIndex = springIndex;
	}

	@Override
	public void provideCompletions(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc, Collection<ICompletionProposal> completions) {

		Optional<IJavaProject> optionalProject = projectFinder.find(doc.getId());
		if (!optionalProject.isPresent()) {
			return;
		}
		
		IJavaProject project = optionalProject.get();
		
		try {
			
			// case: @Qualifier(<*>)
			if (node == annotation && doc.get(offset - 1, 2).endsWith("()")) {
				createCompletionProposals(project, doc, node, completions, offset, offset, "", (beanName) -> "\"" + beanName + "\"");
				
			}
			// case: @Qualifier(prefix<*>)
			else if (node instanceof SimpleName && node.getParent() instanceof Annotation) {
				computeProposalsForSimpleName(project, node, completions, offset, doc);
			}
			// case: @Qualifier(value=<*>)
			else if (node instanceof SimpleName && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				computeProposalsForSimpleName(project, node, completions, offset, doc);
			}
			// case: @Qualifier("prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForStringLiteral(project, node, completions, offset, doc);
				}
			}
			else if (node instanceof StringLiteral && node.getParent() instanceof ArrayInitializer) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForInsideArrayInitializer(project, node, completions, offset, doc);
				}
			}
			// case: @Qualifier(value="prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					computeProposalsForStringLiteral(project, node, completions, offset, doc);
				}
			}
			// case: @Qualifier({<*>})
			else if (node instanceof ArrayInitializer && node.getParent() instanceof Annotation) {
				computeProposalsForArrayInitializr(project, (ArrayInitializer) node, completions, offset, doc);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void computeProposalsForSimpleName(IJavaProject project, ASTNode node, Collection<ICompletionProposal> completions, int offset, TextDocument doc) {
		String prefix = identifyPropertyPrefix(node.toString(), offset - node.getStartPosition());

		int startOffset = node.getStartPosition();
		int endOffset = node.getStartPosition() + node.getLength();

		String proposalPrefix = "\"";
		String proposalPostfix = "\"";

		createCompletionProposals(project, doc, node, completions, startOffset, endOffset, prefix, (beanName) -> proposalPrefix + beanName + proposalPostfix);
	}

	private void computeProposalsForStringLiteral(IJavaProject project, ASTNode node, Collection<ICompletionProposal> completions, int offset, TextDocument doc) throws BadLocationException {
		int length = offset - (node.getStartPosition() + 1);

		String prefix = identifyPropertyPrefix(doc.get(node.getStartPosition() + 1, length), length);
		int startOffset = offset - prefix.length();
		int endOffset = offset;

		createCompletionProposals(project, doc, node, completions, startOffset, endOffset, prefix, (beanName) -> beanName);
	}
	
	private void computeProposalsForArrayInitializr(IJavaProject project, ArrayInitializer node, Collection<ICompletionProposal> completions, int offset, TextDocument doc) {
		createCompletionProposals(project, doc, node, completions, offset, offset, "", (beanName) -> "\"" + beanName + "\"");
	}
	
	private void computeProposalsForInsideArrayInitializer(IJavaProject project, ASTNode node, Collection<ICompletionProposal> completions, int offset, TextDocument doc) throws BadLocationException {
		int length = offset - (node.getStartPosition() + 1);
		if (length >= 0) {
			computeProposalsForStringLiteral(project, node, completions, offset, doc);
		}
		else {
			createCompletionProposals(project, doc, node, completions, offset, offset, "", (beanName) -> "\"" + beanName + "\",");
		}
	}

	private void createCompletionProposals(IJavaProject project, TextDocument doc, ASTNode node, Collection<ICompletionProposal> completions, int startOffset, int endOffset,
			String filterPrefix, Function<String, String> createReplacementText) {

		Set<String> mentionedQualifiers = alreadyMentionedValues(node);

		Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
		Set<String> candidates = Stream.concat(
				findAllQualifiers(beans),
				Arrays.stream(beans).map(bean -> bean.getName()))
				.collect(Collectors.toCollection(LinkedHashSet::new));

		List<String> filteredCandidates = candidates.stream()
			.filter(candidate -> candidate.toLowerCase().startsWith(filterPrefix.toLowerCase()))
			.filter(candidate -> !mentionedQualifiers.contains(candidate))
			.collect(Collectors.toList());
		

		double score = filteredCandidates.size();
		for (String candidate : filteredCandidates) {

			DocumentEdits edits = new DocumentEdits(doc, false);
			edits.replace(startOffset, endOffset, createReplacementText.apply(candidate));

			QualifierCompletionProposal proposal = new QualifierCompletionProposal(edits, candidate, candidate, null, score--);
			completions.add(proposal);
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
	
	private Set<String> alreadyMentionedValues(ASTNode node) {
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
	
	private Stream<String> findAllQualifiers(Bean[] beans) {

		Stream<String> qualifiersFromBeans = Arrays.stream(beans)
				// annotations from beans themselves
				.flatMap(bean -> Arrays.stream(bean.getAnnotations()))
				.filter(annotation -> Annotations.QUALIFIER.equals(annotation.getAnnotationType()))
				.filter(annotation -> annotation.getAttributes() != null && annotation.getAttributes().containsKey("value") && annotation.getAttributes().get("value").length == 1)
				.map(annotation -> annotation.getAttributes().get("value")[0]);
		
		Stream<String> qualifiersFromInjectionPoints = Arrays.stream(beans)
				// annotations from beans themselves
				.filter(bean -> bean.getInjectionPoints() != null)
				.flatMap(bean -> Arrays.stream(bean.getInjectionPoints()))
				.filter(injectionPoint -> injectionPoint.getAnnotations() != null)
				.flatMap(injectionPoint -> Arrays.stream(injectionPoint.getAnnotations()))
				.filter(annotation -> Annotations.QUALIFIER.equals(annotation.getAnnotationType()))
				.filter(annotation -> annotation.getAttributes() != null && annotation.getAttributes().containsKey("value") && annotation.getAttributes().get("value").length == 1)
				.map(annotation -> annotation.getAttributes().get("value")[0]);
		
		return Stream.concat(qualifiersFromBeans, qualifiersFromInjectionPoints);
		
	}



}
