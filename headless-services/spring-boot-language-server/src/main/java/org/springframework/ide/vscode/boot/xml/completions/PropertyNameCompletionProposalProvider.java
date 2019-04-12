/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.completions;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.springframework.ide.vscode.boot.xml.XMLCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
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

		Optional<IJavaProject> foundProject = this.projectFinder.find(doc.getId());
		if (foundProject.isPresent()) {
			IJavaProject project = foundProject.get();

			String prefix = tokenText.substring(0, offset - tokenOffset);
			if (prefix.startsWith("\"")) {
				prefix = prefix.substring(1);
			}

			String beanClass = identifyBeanClass(node);
			if (beanClass != null) {
				IType beanType = project.getIndex().findType(beanClass);

				final String searchPrefix = prefix;
				return beanType.getMethods()
						.filter(method -> isPropertyWriteMethod(method))
						.filter(method -> getPropertyName(method).startsWith(searchPrefix))
						.map(method -> createProposal(method, doc, offset, tokenOffset, tokenEnd))
						.collect(Collectors.toList());
			}
		};

		return Collections.emptyList();
	}

	private String identifyBeanClass(DOMNode node) {
		DOMNode parentNode = node.getParentNode();
		if (parentNode != null) {
			if ("bean".equals(parentNode.getLocalName())) {
				String beanClassAttribute = parentNode.getAttribute("class");
				return beanClassAttribute;
			}
		}
		return null;
	}

	private ICompletionProposal createProposal(IMethod method, TextDocument doc, int offset, int tokenStart, int tokenEnd) {
		String label = getPropertyName(method);
		CompletionItemKind kind = CompletionItemKind.Method;

		DocumentEdits edits = new DocumentEdits(doc);

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

	private boolean isPropertyWriteMethod(IMethod method) {
		return method != null
				&& method.getElementName().startsWith("set")
				&& method.getElementName().length() > 3;
	}

	private String getPropertyName(IMethod method) {
		String methodName = method.getElementName();
		if (methodName.startsWith("set")) {
			String propertyName = methodName.substring(3);
			if (propertyName.length() > 0) {
				return StringUtil.lowerCaseFirstChar(propertyName);
			}
		}
		return methodName;
	}

}
