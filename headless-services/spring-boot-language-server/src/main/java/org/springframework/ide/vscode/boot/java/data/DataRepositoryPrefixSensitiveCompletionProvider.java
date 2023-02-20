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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.util.StringUtils;

/**
 * This utility class provides content assist proposals for Spring JPA query methods.
 * @author danthe1st
 */
class DataRepositoryPrefixSensitiveCompletionProvider {

	private static final Map<String, List<QueryPredicateKeywordInfo>> PREDICATE_KEYWORDS_GROUPED_BY_FIRST_WORD = QueryPredicateKeywordInfo.PREDICATE_KEYWORDS
			.stream()
			.collect(Collectors.groupingBy(info->{
				return findFirstWord(info.keyword());
			}));

	private DataRepositoryPrefixSensitiveCompletionProvider() {
		//prevent instantiation
	}

	private static String findFirstWord(String expression) {
		int firstWordEnd;
		for (firstWordEnd = 1;
				firstWordEnd < expression.length()
				&& Character.isLowerCase(expression.charAt(firstWordEnd));
				firstWordEnd++) {
			//search is done in loop condition
		}
		return expression.substring(0, firstWordEnd);
	}

	static void addPrefixSensitiveProposals(Collection<ICompletionProposal> completions, int offset, String prefix, DataRepositoryDefinition repoDef){
		String localPrefix = findJavaIdentifierPart(prefix);
		addQueryStartProposals(completions, localPrefix, offset);
		if (localPrefix == null) {
			return;
		}
		DataRepositoryMethodNameParseResult parseResult = parseLocalPrefixForCompletion(localPrefix, repoDef);
		if (parseResult != null) {
			if(parseResult.performFullCompletion()) {
				String methodName=localPrefix;
				DocumentEdits edits = new DocumentEdits(null, false);
				String signature = parseResult
						.parameters()
						.stream()
						.map(param -> {
							DomainProperty[] properties = repoDef.getDomainType().getProperties();
							for(DomainProperty domainProperty : properties){
								if(domainProperty.getName().equalsIgnoreCase(param)) {
									return domainProperty.getType().getSimpleName() + " " + StringUtils.uncapitalize(param);
								}
							}
							return "Object " + StringUtils.uncapitalize(param);
						})
						.collect(Collectors.joining(", ", methodName + "(",")"));
				StringBuilder newText = new StringBuilder();
				newText.append(parseResult.subjectType().returnType());
				if (parseResult.subjectType().isTyped()) {
					newText.append("<");
					newText.append(repoDef.getDomainType().getSimpleName());
					newText.append(">");
				}
				newText.append(" ");
				newText.append(signature);
				newText.append(";");
				edits.replace(offset - localPrefix.length(), offset, newText.toString());
				DocumentEdits additionalEdits = new DocumentEdits(null, false);
				ICompletionProposal proposal = new FindByCompletionProposal(methodName, CompletionItemKind.Method, edits, null, null, Optional.of(additionalEdits), signature);
				completions.add(proposal);
			}
		}
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

	private static DataRepositoryMethodNameParseResult parseLocalPrefixForCompletion(String localPrefix, DataRepositoryDefinition repoDef) {
		Map<String, List<DomainProperty>> propertiesGroupedByFirstWord = groupPropertiesByFirstWord(repoDef);

		propertiesGroupedByFirstWord.toString();
		int subjectPredicateSplitIndex = localPrefix.indexOf("By");
		if (subjectPredicateSplitIndex == -1) {
			return null;
		}
		String subject=localPrefix.substring(0,subjectPredicateSplitIndex);
		QueryMethodSubject subjectType = null;
		for(QueryMethodSubject queryMethodSubject : QueryMethodSubject.QUERY_METHOD_SUBJECTS){
			if(subject.startsWith(queryMethodSubject.key())) {
				subjectType = queryMethodSubject;
			}
		}
		if (subjectType == null) {
			return null;
		}
		String predicate = localPrefix.substring(subjectPredicateSplitIndex + 2);
		List<String> parameters=new ArrayList<>();
		String previousExpression = null;
		int lastWordEnd = 0;
		String expectedNextType = null;//the expected type as string if a type is expected, if the type cannot be found, the user should supply it

		boolean performFullCompletion = true;//if some invalid text is detected, do not complete the whole method
		for (int i = 1; i <= predicate.length(); i++) {
			if(i == predicate.length() || Character.isUpperCase(predicate.charAt(i))) {//word ends on uppercase letter or end of string
				String word = predicate.substring(lastWordEnd, i);
				QueryPredicateKeywordInfo keyword = findByLargestFirstWord(PREDICATE_KEYWORDS_GROUPED_BY_FIRST_WORD, QueryPredicateKeywordInfo::keyword, predicate, lastWordEnd, word);
				if (keyword != null) {//word is keyword
					i += keyword.keyword().length()-word.length();
					switch(keyword.type()) {
						case TERMINATE_EXPRESSION: {//e.g. IsTrue
							if (expectedNextType == null) {
								//if no next type/expression is expected (which should not happen), do not complete the full method (parameters)
								performFullCompletion = false;
							}
							expectedNextType = null;

							break;
						}
						case COMBINE_CONDITIONS: {//e.g. And
							//if an expression is expected, it is added to the parameters
							if (expectedNextType != null) {
								parameters.add(expectedNextType);
							}
							expectedNextType = null;
							break;
						}
						case COMPARE: {//e.g. GreaterThan
							if (expectedNextType == null) {
								//nothing to compare, e.g. And directly followed by GreaterThan
								performFullCompletion = false;
							}
							expectedNextType = previousExpression;
							break;
						}
						case IGNORE:{
							//ignore
							break;
						}
						default:
							throw new IllegalArgumentException("Unexpected value: " + keyword.type());
					}
					previousExpression = null;
				} else {
					DomainProperty preferredWord = findByLargestFirstWord(propertiesGroupedByFirstWord, DomainProperty::getName, predicate, lastWordEnd, word);
					if (preferredWord != null) {
						i += preferredWord.getName().length()-word.length();
						word=preferredWord.getName();
					}
					if (previousExpression == null){
						previousExpression = word;
						//non-keywords just invert the status
						//if an expression is expected, the word is the expression
						//if not, some expression is required after the word
						if (expectedNextType == null) {
							expectedNextType = word;
						} else {
							expectedNextType = null;
						}
					} else {
						//combine multiple words that are not keywords
						previousExpression += word;
						if (expectedNextType != null) {
							expectedNextType = previousExpression;
						}
					}
				}
				lastWordEnd = i;
			}
		}
		if (expectedNextType != null) {
			parameters.add(expectedNextType);
		}

		EnumSet<DataRepositoryMethodKeywordType> allowedKeywordTypes = EnumSet.allOf(DataRepositoryMethodKeywordType.class);
		if (expectedNextType == null) {
			allowedKeywordTypes.remove(DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION);
			allowedKeywordTypes.remove(DataRepositoryMethodKeywordType.COMPARE);
		}
		return new DataRepositoryMethodNameParseResult(subjectType, parameters, performFullCompletion, previousExpression, allowedKeywordTypes);
	}

	private static <T> T findByLargestFirstWord(Map<String, List<T>> toSearch, Function<T, String> expressionExtractor, String predicate, int lastWordEnd, String word) {
		T ret = null;
		if (toSearch.containsKey(word)) {
			for(T possibleKeyword : toSearch.get(word)){
				int endPosition = lastWordEnd + expressionExtractor.apply(possibleKeyword).length();
				if (predicate.length() >= endPosition
						&& expressionExtractor.apply(possibleKeyword).equals(predicate.substring(lastWordEnd, endPosition))
						&& (ret == null || expressionExtractor.apply(possibleKeyword).length() > expressionExtractor.apply(possibleKeyword).length())) {//find largest valid keyword
					ret = possibleKeyword;
				}
			}
		}
		return ret;
	}

	private static Map<String, List<DomainProperty>> groupPropertiesByFirstWord(DataRepositoryDefinition repoDef) {
		Map<String, List<DomainProperty>> propertiesGroupedByFirstWord = new HashMap<>();
		for(DomainProperty property : repoDef.getDomainType().getProperties()){
			String firstWord = findFirstWord(property.getName());
			propertiesGroupedByFirstWord.putIfAbsent(firstWord, new ArrayList<>());
			propertiesGroupedByFirstWord.get(firstWord).add(property);
		}
		return propertiesGroupedByFirstWord;
	}
}
