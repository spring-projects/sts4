/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lsp4j.CompletionItemKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.xml.XMLCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class PropertyNameCompletionProposalProvider implements XMLCompletionProvider {
	
	private static final Logger log = LoggerFactory.getLogger(PropertyNameCompletionProposalProvider.class);

	private final JavaProjectFinder projectFinder;

	public PropertyNameCompletionProposalProvider(JavaProjectFinder projectFinder) {
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
				return propertyNameCandidateMethods(project, beanClass)
					.filter(method -> getPropertyName(method).startsWith(searchPrefix))
					.map(method -> createProposal(method, doc, offset, tokenOffset, tokenEnd))
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

	private ICompletionProposal createProposal(IMethod method, TextDocument doc, int offset, int tokenStart, int tokenEnd) {
		String label = getPropertyName(method);
		CompletionItemKind kind = CompletionItemKind.Method;

		DocumentEdits edits = new DocumentEdits(doc, false);

		String replaceString = "\"" + label + "\"";
		int replaceStart = tokenStart;

		if (tokenStart < offset) {
			replaceStart = offset;
			replaceString = replaceString.substring(offset - tokenStart);
		}
		edits.replace(replaceStart, tokenEnd, replaceString);

		Renderable renderable = null;

		return new TypeCompletionProposal(label, kind, edits, label, renderable, 1d);
	}

	private static boolean isPropertyWriteMethod(IMethod method) {
		return method != null
				&& method.getElementName().startsWith("set")
				&& method.getElementName().length() > 3;
	}

	public static String getPropertyName(IMethod method) {
		String methodName = method.getElementName();
		if (methodName.startsWith("set")) {
			String propertyName = methodName.substring(3);
			if (propertyName.length() > 0) {
				return StringUtil.lowerCaseFirstChar(propertyName);
			}
		}
		return methodName;
	}
	
	public static Stream<IMethod> propertyNameCandidateMethods(IJavaProject project, String beanClassFqName) {
		return project.getIndex().allSuperTypesOf(beanClassFqName, true, true)
			.toStream()
			.flatMap(type -> type.getMethods())
			.filter(PropertyNameCompletionProposalProvider::isPropertyWriteMethod);
	}

}
