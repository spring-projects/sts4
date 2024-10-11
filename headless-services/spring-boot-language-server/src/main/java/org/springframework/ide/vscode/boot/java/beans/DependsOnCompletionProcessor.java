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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationAttributeCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * @author Martin Lippert
 */
public class DependsOnCompletionProcessor implements AnnotationAttributeCompletionProvider {

	private final SpringMetamodelIndex springIndex;

	public DependsOnCompletionProcessor(SpringMetamodelIndex springIndex) {
		this.springIndex = springIndex;
	}

//	@Override
//	public void provideCompletions(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc, Collection<ICompletionProposal> completions) {
//
//		Optional<IJavaProject> optionalProject = projectFinder.find(doc.getId());
//		if (!optionalProject.isPresent()) {
//			return;
//		}
//		
//		IJavaProject project = optionalProject.get();
//		
//		try {
//			
//			// case: @DependsOn(<*>)
//			if (node == annotation && doc.get(offset - 1, 2).endsWith("()")) {
//				Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
//				
//				for (Bean bean : beans) {
//
//					DocumentEdits edits = new DocumentEdits(doc, false);
//					edits.replace(offset, offset, "\"" + bean.getName() + "\"");
//
//					DependsOnCompletionProposal proposal = new DependsOnCompletionProposal(edits, bean.getName(), bean.getName(), null);
//
//					completions.add(proposal);
//				}
//			}
//			// case: @DependsOn(prefix<*>)
//			else if (node instanceof SimpleName && node.getParent() instanceof Annotation) {
//				computeProposalsForSimpleName(project, node, completions, offset, doc);
//			}
//			// case: @DependsOn(value=<*>)
//			else if (node instanceof SimpleName && node.getParent() instanceof MemberValuePair
//					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
//				computeProposalsForSimpleName(project, node, completions, offset, doc);
//			}
//			// case: @DependsOn("prefix<*>")
//			else if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
//				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
//					computeProposalsForStringLiteral(project, node, completions, offset, doc);
//				}
//			}
//			else if (node instanceof StringLiteral && node.getParent() instanceof ArrayInitializer) {
//				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
//					computeProposalsForInsideArrayInitializer(project, node, completions, offset, doc);
//				}
//			}
//			// case: @DependsOn(value="prefix<*>")
//			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
//					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
//				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
//					computeProposalsForStringLiteral(project, node, completions, offset, doc);
//				}
//			}
//			// case: @DependsOn({<*>})
//			else if (node instanceof ArrayInitializer && node.getParent() instanceof Annotation) {
//				computeProposalsForArrayInitializr(project, (ArrayInitializer) node, completions, offset, doc);
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void computeProposalsForSimpleName(IJavaProject project, ASTNode node, Collection<ICompletionProposal> completions, int offset, IDocument doc) {
//		String prefix = identifyPropertyPrefix(node.toString(), offset - node.getStartPosition());
//
//		int startOffset = node.getStartPosition();
//		int endOffset = node.getStartPosition() + node.getLength();
//
//		String proposalPrefix = "\"";
//		String proposalPostfix = "\"";
//		
//		Set<String> mentionedBeans = alreadyMentionedBeans(node);
//		
//		Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
//		List<Bean> matchingBeans = Arrays.stream(beans)
//			.filter(bean -> bean.getName().toLowerCase().startsWith(prefix.toLowerCase()))
//			.filter(bean -> !mentionedBeans.contains(bean.getName()))
//			.collect(Collectors.toList());
//
//		for (Bean bean : matchingBeans) {
//
//			DocumentEdits edits = new DocumentEdits(doc, false);
//			edits.replace(startOffset, endOffset, proposalPrefix + bean.getName() + proposalPostfix);
//
//			DependsOnCompletionProposal proposal = new DependsOnCompletionProposal(edits, bean.getName(), bean.getName(), null);
//
//			completions.add(proposal);
//		}
//	}
//
//	private void computeProposalsForStringLiteral(IJavaProject project, ASTNode node, Collection<ICompletionProposal> completions, int offset, IDocument doc) throws BadLocationException {
//		int length = offset - (node.getStartPosition() + 1);
//
//		String prefix = identifyPropertyPrefix(doc.get(node.getStartPosition() + 1, length), length);
//		int startOffset = offset - prefix.length();
//		int endOffset = offset;
//		
//		Set<String> mentionedBeans = alreadyMentionedBeans(node);
//
//		Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
//
//		final String filterPrefix = prefix;
//		List<Bean> matchingBeans = Arrays.stream(beans)
//			.filter(bean -> bean.getName().toLowerCase().startsWith(filterPrefix.toLowerCase()))
//			.filter(bean -> !mentionedBeans.contains(bean.getName()))
//			.collect(Collectors.toList());
//
//		for (Bean bean : matchingBeans) {
//
//			DocumentEdits edits = new DocumentEdits(doc, false);
//			edits.replace(startOffset, endOffset, bean.getName());
//
//			DependsOnCompletionProposal proposal = new DependsOnCompletionProposal(edits, bean.getName(), bean.getName(), null);
//
//			completions.add(proposal);
//		}
//	}
//	
//	private void computeProposalsForArrayInitializr(IJavaProject project, ArrayInitializer node, Collection<ICompletionProposal> completions, int offset, IDocument doc) {
//		Set<String> mentionedBeans = alreadyMentionedBeans(node);
//
//		Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
//		List<Bean> filteredBeans = Arrays.stream(beans)
//			.filter(bean -> !mentionedBeans.contains(bean.getName()))
//			.collect(Collectors.toList());
//		
//		for (Bean bean : filteredBeans) {
//
//			DocumentEdits edits = new DocumentEdits(doc, false);
//			edits.replace(offset, offset, "\"" + bean.getName() + "\"");
//
//			DependsOnCompletionProposal proposal = new DependsOnCompletionProposal(edits, bean.getName(), bean.getName(), null);
//
//			completions.add(proposal);
//		}
//	}
//	
//	private void computeProposalsForInsideArrayInitializer(IJavaProject project, ASTNode node, Collection<ICompletionProposal> completions, int offset, TextDocument doc) throws BadLocationException {
//		int length = offset - (node.getStartPosition() + 1);
//		if (length >= 0) {
//			computeProposalsForStringLiteral(project, node, completions, offset, doc);
//		}
//		else {
//			Set<String> mentionedBeans = alreadyMentionedBeans(node);
//
//			Bean[] beans = this.springIndex.getBeansOfProject(project.getElementName());
//			List<Bean> filteredBeans = Arrays.stream(beans)
//				.filter(bean -> !mentionedBeans.contains(bean.getName()))
//				.collect(Collectors.toList());
//			
//			for (Bean bean : filteredBeans) {
//
//				DocumentEdits edits = new DocumentEdits(doc, false);
//				edits.replace(offset, offset, "\"" + bean.getName() + "\",");
//
//				DependsOnCompletionProposal proposal = new DependsOnCompletionProposal(edits, bean.getName(), bean.getName(), null);
//
//				completions.add(proposal);
//			}
//		}
//	}
//	
//	private String identifyPropertyPrefix(String nodeContent, int offset) {
//		String result = nodeContent.substring(0, offset);
//
//		int i = offset - 1;
//		while (i >= 0) {
//			char c = nodeContent.charAt(i);
//			if (c == '}' || c == '{'  || c == '$' || c == '#') {
//				result = result.substring(i + 1, offset);
//				break;
//			}
//			i--;
//		}
//
//		return result;
//	}
//	
//	private Set<String> alreadyMentionedBeans(ASTNode node) {
//		Set<String> result = new HashSet<>();
//		
//		ArrayInitializer arrayNode = null;
//		while (node != null && arrayNode == null && !(node instanceof Annotation)) {
//			if (node instanceof ArrayInitializer) {
//				arrayNode = (ArrayInitializer) node;
//			}
//			else {
//				node = node.getParent();
//			}
//		}
//		
//		if (arrayNode != null) {
//			List<?> expressions = arrayNode.expressions();
//			for (Object expression : expressions) {
//				if (expression instanceof StringLiteral) {
//					StringLiteral stringExr = (StringLiteral) expression;
//					String value = stringExr.getLiteralValue();
//					result.add(value);
//				}
//			}
//		}
//		
//		return result;
//	}

	@Override
	public Map<String, String> getCompletionCandidates(IJavaProject project, ASTNode node) {
		return Arrays.stream(this.springIndex.getBeansOfProject(project.getElementName()))
				.map(bean -> bean.getName())
				.distinct()
				.collect(Collectors.toMap(key -> key, value -> value, (u, v) -> u, LinkedHashMap::new));
	}



}
