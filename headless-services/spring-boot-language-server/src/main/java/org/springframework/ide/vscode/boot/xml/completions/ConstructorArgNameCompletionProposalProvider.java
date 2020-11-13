/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.completions;

import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.BEAN_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.CLASS_ATTRIBUTE;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lsp4j.CompletionItemKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.metadata.types.Type;
import org.springframework.ide.vscode.boot.xml.XMLCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class ConstructorArgNameCompletionProposalProvider implements XMLCompletionProvider {
	
	private static final Logger log = LoggerFactory.getLogger(ConstructorArgNameCompletionProposalProvider.class);

	private final JavaProjectFinder projectFinder;

	public ConstructorArgNameCompletionProposalProvider(JavaProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(TextDocument doc, String namespace, DOMNode node, DOMAttr attributeAt,
			Scanner scanner, int offset) {

		int tokenOffset = scanner.getTokenOffset();
		int tokenEnd = scanner.getTokenEnd();
		String tokenText = scanner.getTokenText();
		
		log.info("Stating calculating completions for {} at offset {}.", doc.getId().getUri(), offset);


		Optional<IJavaProject> foundProject = this.projectFinder.find(doc.getId());
		if (foundProject.isPresent()) {
			IJavaProject project = foundProject.get();
			log.info("Project found is {}", project.getElementName());

			String prefix = tokenText.substring(0, offset - tokenOffset);
			if (prefix.startsWith("\"")) {
				prefix = prefix.substring(1);
			}

			log.info("Prefix is '{}'", prefix);

			String beanClass = identifyBeanClass(node);
			if (beanClass != null && beanClass.length() > 0) {
				
				log.info("Bean class '{}'", beanClass);

				final String searchPrefix = prefix;
				return constructorArgNameCandidates(project, beanClass)
					.filter(constructorArg -> constructorArg.getRight().startsWith(searchPrefix))
					.map(constructorArg -> createProposal(constructorArg, doc, offset, tokenOffset, tokenEnd))
					.collect(Collectors.toList());
			}
		};

		return Collections.emptyList();
	}

	public static String identifyBeanClass(DOMNode node) {
		DOMNode parentNode = node.getParentNode();
		if (parentNode != null) {
			if (BEAN_ELEMENT.equals(parentNode.getLocalName())) {
				String beanClassAttribute = parentNode.getAttribute(CLASS_ATTRIBUTE);
				return beanClassAttribute;
			}
		}
		return null;
	}

	private ICompletionProposal createProposal(Pair<IJavaType, String> constructorArg, TextDocument doc, int offset, int tokenStart, int tokenEnd) {
		String label = constructorArg.getRight();
		CompletionItemKind kind = CompletionItemKind.Reference;

		DocumentEdits edits = new DocumentEdits(doc, false);

		String replaceString = "\"" + label + "\"";
		int replaceStart = tokenStart;

		if (tokenStart < offset) {
			replaceStart = offset;
			replaceString = replaceString.substring(offset - tokenStart);
		}
		edits.replace(replaceStart, tokenEnd, replaceString);

		Renderable renderable = null;
		String detail = label;
		
		IJavaType paramType = constructorArg.getLeft();
		if (paramType != null) {
			Type type = Type.fromJavaType(paramType);
			if (type != null) {
				detail = constructorArg.getRight() + " - " + type.toString();
			}
		}

		return new GenericXMLCompletionProposal(label, kind, edits, detail, renderable, 1d);
	}

	public static Stream<Pair<IJavaType, String>> constructorArgNameCandidates(IJavaProject project, String beanClassFqName) {
		IType type = project.getIndex().findType(beanClassFqName);
		if (type != null) {
			return type.getMethods()
					.filter(method -> method.isConstructor())
					.flatMap(method -> getConstructorArgs(method));
		}
		else {
			return Stream.empty();
		}
	}

	private static Stream<Pair<IJavaType, String>> getConstructorArgs(IMethod method) {
		List<String> parameterNames = method.getParameterNames();
		List<IJavaType> parameterTypes = method.parameters().collect(Collectors.toList());
		
		if (parameterNames != null && parameterTypes != null && parameterNames.size() == parameterTypes.size()) {
			return IntStream.range(0, parameterNames.size())
				.mapToObj(i -> Pair.of(parameterTypes.get(i), parameterNames.get(i)));
		}
		else {
			return Stream.empty();
		}
	}

}
