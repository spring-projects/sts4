/*******************************************************************************
 * Copyright (c) 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.util.StringUtils;

/**
 * This utility class provides content assist proposals for Spring JPA query methods.
 * @author danthe1st
 */
class DataRepositoryPrefixSensitiveCompletionProvider {

	private DataRepositoryPrefixSensitiveCompletionProvider() {
		//prevent instantiation
	}

	static void addPrefixSensitiveProposals(Collection<ICompletionProposal> completions, int offset, String prefix, DataRepositoryDefinition repoDef){
		String localPrefix = findJavaIdentifierPart(prefix);
		addQueryStartProposals(completions, localPrefix, offset);
		if (localPrefix == null) {
			return;
		}
		DataRepositoryMethodNameParseResult parseResult = new JPARepositoryMethodParser(localPrefix, repoDef).parseLocalPrefixForCompletion();
		if(parseResult != null && parseResult.performFullCompletion()){
			Map<String, DomainProperty> propertiesByName = getPropertiesByName(repoDef.getDomainType().getProperties());
			addMethodCompletionProposal(completions, offset, repoDef, localPrefix, prefix, parseResult, propertiesByName);

			if (parseResult.lastWord() == null || !propertiesByName.containsKey(parseResult.lastWord())) {
				addPropertyProposals(completions, offset, repoDef, parseResult);
			}
		}
	}

	private static void addPropertyProposals(Collection<ICompletionProposal> completions, int offset, DataRepositoryDefinition repoDef, DataRepositoryMethodNameParseResult parseResult) {
		for(DomainProperty property : repoDef.getDomainType().getProperties()){
			String lastWord = parseResult.lastWord();
			if (lastWord == null) {
				lastWord = "";
			}
			if (property.getName().startsWith(lastWord)) {
				DocumentEdits edits = new DocumentEdits(null, false);
				edits.replace(offset - lastWord.length(), offset, property.getName());
				DocumentEdits additionalEdits = new DocumentEdits(null, false);
				ICompletionProposal proposal = new FindByCompletionProposal(property.getName(), CompletionItemKind.Text, edits, "property " + property.getName(), null, Optional.of(additionalEdits), lastWord);
				completions.add(proposal);
			}
		}
	}

	private static void addMethodCompletionProposal(Collection<ICompletionProposal> completions, int offset, DataRepositoryDefinition repoDef, String localPrefix, String fullPrefix, DataRepositoryMethodNameParseResult parseResult, Map<String, DomainProperty> propertiesByName) {
		String methodName = localPrefix;
		DocumentEdits edits = new DocumentEdits(null, false);
		String signature = buildSignature(methodName, propertiesByName, parseResult);
		StringBuilder newText = new StringBuilder();
		newText.append(parseResult.subjectType().returnType());
		if (parseResult.subjectType().isTyped()) {
			newText.append("<");
			newText.append(repoDef.getDomainType().getSimpleName());
			newText.append(">");
		}
		String returnType = newText.toString();
		newText.append(" ");
		newText.append(signature);
		newText.append(";");
		int replaceStart = calculateReplaceOffset(offset, localPrefix, fullPrefix, returnType);
		edits.replace(replaceStart, offset, newText.toString());
		DocumentEdits additionalEdits = new DocumentEdits(null, false);
		ICompletionProposal proposal = new FindByCompletionProposal(methodName, CompletionItemKind.Method, edits, null, null, Optional.of(additionalEdits), signature);
		completions.add(proposal);
	}

	private static int calculateReplaceOffset(int offset, String localPrefix, String fullPrefix, String returnType) {
		int replaceStart = offset - localPrefix.length();
		String beforeLocalPrefix = fullPrefix.substring(0, fullPrefix.length()-localPrefix.length());
		String trimmed = beforeLocalPrefix.trim();
		if(trimmed.endsWith(returnType)) {
			replaceStart -= (beforeLocalPrefix.length() - trimmed.length()) + returnType.length();
		}
		return replaceStart;
	}

	private static Map<String, DomainProperty> getPropertiesByName(DomainProperty[] properties) {
		Map<String, DomainProperty> propertiesByName = new HashMap<>();
		for(DomainProperty prop : properties){
			propertiesByName.put(prop.getName(), prop);
		}
		return propertiesByName;
	}

	private static String buildSignature(String methodName, Map<String, DomainProperty> properties, DataRepositoryMethodNameParseResult parseResult) {
		StringBuilder signatureBuilder = new StringBuilder();
		signatureBuilder.append(methodName);
		signatureBuilder.append("(");
		List<String> parameters = parseResult.parameters();
		for(int i = 0; i < parameters.size(); i++){
			String param = parameters.get(i);
			if (properties.containsKey(param)) {
				signatureBuilder.append(properties.get(param).getType().getSimpleName());
			} else {
				signatureBuilder.append("Object");
			}
			signatureBuilder.append(" ");
			signatureBuilder.append(StringUtils.uncapitalize(param));
			if (i + 1 < parameters.size()) {
				signatureBuilder.append(", ");
			}
		}
		signatureBuilder.append(")");
		return signatureBuilder.toString();
	}

	private static void addQueryStartProposals(Collection<ICompletionProposal> completions, String prefix, int offset) {
		for(QueryMethodSubject queryMethodSubject : QueryMethodSubject.QUERY_METHOD_SUBJECTS){
			String toInsert = queryMethodSubject.key() + "By";
			completions.add(DataRepositoryCompletionProcessor.createProposal(offset, CompletionItemKind.Text, prefix, toInsert, toInsert));
		}
	}

	private static String findJavaIdentifierPart(String prefix) {
		if (prefix == null) {
			return null;
		}
		int lastNonIdentifierPartIndex;
		for (lastNonIdentifierPartIndex = prefix.length() - 1; lastNonIdentifierPartIndex >= 0 && Character.isJavaIdentifierPart(prefix.charAt(lastNonIdentifierPartIndex)); lastNonIdentifierPartIndex--) {
			// search done using loop condition
		}
		return prefix.substring(lastNonIdentifierPartIndex + 1);
	}


}
