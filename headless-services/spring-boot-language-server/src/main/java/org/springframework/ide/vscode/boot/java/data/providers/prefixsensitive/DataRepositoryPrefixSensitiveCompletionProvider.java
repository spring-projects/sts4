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
package org.springframework.ide.vscode.boot.java.data.providers.prefixsensitive;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.boot.java.data.DataRepositoryDefinition;
import org.springframework.ide.vscode.boot.java.data.DomainProperty;
import org.springframework.ide.vscode.boot.java.data.DomainType;
import org.springframework.ide.vscode.boot.java.data.FindByCompletionProposal;
import org.springframework.ide.vscode.boot.java.data.providers.DataRepositoryCompletionProvider;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.util.StringUtils;

/**
 * This class provides content assist proposals for Spring Data repository query methods.
 * @author danthe1st
 */
public class DataRepositoryPrefixSensitiveCompletionProvider implements DataRepositoryCompletionProvider {

	@Override
	public void addProposals(Collection<ICompletionProposal> completions, IDocument doc, int offset, String prefix, DataRepositoryDefinition repoDef) {
		String localPrefix = findLastJavaIdentifierPart(prefix);
		if (localPrefix == null) {
			return;
		}
		DataRepositoryMethodNameParseResult parseResult = new DataRepositoryMethodParser(localPrefix, repoDef).parseLocalPrefixForCompletion();
		if(parseResult != null && parseResult.performFullCompletion()){
			Map<String, DomainProperty> propertiesByName = getPropertiesByName(repoDef.getDomainType().getProperties());
			addMethodCompletionProposal(completions, offset, repoDef, localPrefix, prefix, parseResult, propertiesByName);

			if (parseResult.lastWord() == null || !propertiesByName.containsKey(parseResult.lastWord())) {
				addPropertyProposals(completions, offset, repoDef, parseResult);
			}
		}
	}

	private void addPropertyProposals(Collection<ICompletionProposal> completions, int offset, DataRepositoryDefinition repoDef, DataRepositoryMethodNameParseResult parseResult) {
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

	private void addMethodCompletionProposal(Collection<ICompletionProposal> completions, int offset, DataRepositoryDefinition repoDef, String localPrefix, String fullPrefix, DataRepositoryMethodNameParseResult parseResult, Map<String, DomainProperty> propertiesByName) {
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

	private int calculateReplaceOffset(int offset, String localPrefix, String fullPrefix, String returnType) {
		int replaceStart = offset - localPrefix.length();
		String beforeLocalPrefix = fullPrefix.substring(0, fullPrefix.length()-localPrefix.length());
		String trimmed = beforeLocalPrefix.trim();
		if(trimmed.endsWith(returnType)) {
			replaceStart -= (beforeLocalPrefix.length() - trimmed.length()) + returnType.length();
		}
		return replaceStart;
	}

	private Map<String, DomainProperty> getPropertiesByName(DomainProperty[] properties) {
		Map<String, DomainProperty> propertiesByName = new HashMap<>();
		for(DomainProperty prop : properties){
			propertiesByName.put(prop.getName(), prop);
		}
		return propertiesByName;
	}

	private String buildSignature(String methodName, Map<String, DomainProperty> properties, DataRepositoryMethodNameParseResult parseResult) {
		StringBuilder signatureBuilder = new StringBuilder();
		signatureBuilder.append(methodName);
		signatureBuilder.append("(");
		List<String> parameters = parseResult.parameters();
		for(int i = 0; i < parameters.size(); i++){
			String param = parameters.get(i);
			DomainType type = findExpressionType(properties, param);
			if (type == null) {
				signatureBuilder.append("Object");
			}else {
				signatureBuilder.append(type.getSimpleName());
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

	private DomainType findExpressionType(Map<String, DomainProperty> properties, String param) {
		String[] splitByUnderscore = param.split("_");
		if(properties.containsKey(splitByUnderscore[0])) {
			DomainType type = properties.get(splitByUnderscore[0]).getType();
			for (int j = 1; j < splitByUnderscore.length && type != null; j++) {
				type = findMatchingParameter(splitByUnderscore[j], type);
			}
			return type;
		}
		return null;
	}

	private DomainType findMatchingParameter(String name, DomainType type) {
		for(DomainProperty prop : type.getProperties()){
			if (prop.getName().equals(name)) {
				return prop.getType();
			}
		}
		return null;
	}

	public static String findLastJavaIdentifierPart(String prefix) {
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
