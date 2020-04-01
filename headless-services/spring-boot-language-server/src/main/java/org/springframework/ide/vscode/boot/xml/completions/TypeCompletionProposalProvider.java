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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lsp4j.CompletionItemKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.xml.XMLCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.java.JavaCodeCompleteData;
import org.springframework.ide.vscode.commons.protocol.java.JavaCodeCompleteParams;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class TypeCompletionProposalProvider implements XMLCompletionProvider {
	
	private static final Logger log = LoggerFactory.getLogger(TypeCompletionProposalProvider.class);

	private final JavaProjectFinder projectFinder;
	private final SimpleLanguageServer server;
	private final boolean packagesAllowed;
	private final boolean classesAllowed;
	private final boolean interfacesAllowed;
	private final boolean enumsAllowed;

	public TypeCompletionProposalProvider(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			boolean packagesAllowed, boolean classesAllowed, boolean interfacesAllowed, boolean enumsAllowed) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.packagesAllowed = packagesAllowed;
		this.classesAllowed = classesAllowed;
		this.interfacesAllowed = interfacesAllowed;
		this.enumsAllowed = enumsAllowed;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(TextDocument doc, String namespace, DOMNode node, DOMAttr attributeAt,
			Scanner scanner, int offset) {

		int tokenOffset = scanner.getTokenOffset();
		int tokenEnd = scanner.getTokenEnd();
		String tokenText = scanner.getTokenText();
		
		log.info("Stating calculating completions for {} at offset {}.", doc.getId().getUri(), offset);
		log.info("Allowed proposals packagesAllowed={} classesAllowed={} interfacesAllowed={} enumsAllowed={}", packagesAllowed, classesAllowed, interfacesAllowed, enumsAllowed);

		Optional<IJavaProject> foundProject = this.projectFinder.find(doc.getId());
		if (foundProject.isPresent()) {
			log.info("Project found is {}", foundProject.get().getElementName());

			IJavaProject project = foundProject.get();

			String prefix = tokenText.substring(0, offset - tokenOffset);
			if (prefix.startsWith("\"")) {
				prefix = prefix.substring(1);
			}

//			Flux<Tuple2<IType, Double>> types = project.getIndex().fuzzySearchTypes(prefix, true, true);
//			Flux<Tuple2<IType, Double>> types = project.getIndex().camelcaseSearchTypes(prefix, true, true);
			
			log.info("Prefix is '{}'", prefix);
			
			JavaCodeCompleteParams params = new JavaCodeCompleteParams(project.getLocationUri().toString(), prefix, true, true);
			CompletableFuture<List<JavaCodeCompleteData>> completions = server.getClient().javaCodeComplete(params);

			final String finalPrefix = prefix;
			
			try {
				List<JavaCodeCompleteData> list = completions.get();
				return list.stream()
						.filter(proposal -> {
							if (proposal.isClassProposal()) return classesAllowed;
							if (proposal.isInterfaceProposal()) return interfacesAllowed;
							if (proposal.isEnumProposal()) return enumsAllowed;
							if (proposal.isPackageProposal()) return packagesAllowed;
							return false;
						})
						.map(proposal -> createProposal(proposal, doc, finalPrefix, offset))
						.filter(proposal -> proposal != null)
						.collect(Collectors.toList());
			}
			catch (Exception e) {
				log.error("{}", e);
			}
		};

		return Collections.emptyList();
	}

	private ICompletionProposal createProposal(JavaCodeCompleteData proposal, TextDocument doc, String prefix, int offset) {
		if (proposal.isPackageProposal()) {
			return createPackageProposal(proposal, doc, prefix, offset);
		}
		else if (proposal.isClassProposal() || proposal.isInterfaceProposal() || proposal.isEnumProposal()) {
			return createTypeProposal(proposal, doc, prefix, offset);
		}
		else {
			return null;
		}
	}
	
	private TypeCompletionProposal createPackageProposal(JavaCodeCompleteData proposal, TextDocument doc, String prefix, int offset) {
		String fqName = proposal.getFullyQualifiedName();
		String label = fqName;
		CompletionItemKind kind = CompletionItemKind.Module;

		DocumentEdits edits = new DocumentEdits(doc, false);
		if (fqName.startsWith(prefix)) {
			edits.insert(offset, fqName.substring(prefix.length()));
		} else {
			edits.replace(offset - prefix.length(), offset, fqName);
		}

		Renderable renderable = null;

		return new TypeCompletionProposal(label, kind, edits, fqName, renderable, proposal.getRelevance());
	}
	
	private TypeCompletionProposal createTypeProposal(JavaCodeCompleteData proposal, TextDocument doc, String prefix, int offset) {
		String fqName = proposal.getFullyQualifiedName();
		String label = fqName;

		int splitIndex = Math.max(label.lastIndexOf("."), label.lastIndexOf("$"));

		if (splitIndex > 0) {
			label = label.substring(splitIndex + 1) + " - " + label.substring(0, splitIndex);
		}

		CompletionItemKind kind = CompletionItemKind.Class;
		if (proposal.isInterfaceProposal()) {
			kind = CompletionItemKind.Interface;
		}
		else if (proposal.isEnumProposal()) {
			kind = CompletionItemKind.Enum;
		}

		DocumentEdits edits = new DocumentEdits(doc, false);
		if (fqName.startsWith(prefix)) {
			edits.insert(offset, fqName.substring(prefix.length()));
		} else {
			edits.replace(offset - prefix.length(), offset, fqName);
		}

		Renderable renderable = null;

		return new TypeCompletionProposal(label, kind, edits, fqName, renderable, proposal.getRelevance());
	}
		
}
