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

import java.util.List;

/**
 * Represents information about the predicate in Spring JPA repository query methods.
 *
 * See https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#appendix.query.method.predicate
 *
 * @author danthe1st
 */
record QueryPredicateKeywordInfo(String keyword, DataRepositoryMethodKeywordType type) {
	static final List<QueryPredicateKeywordInfo> PREDICATE_KEYWORDS = List.of(
			new QueryPredicateKeywordInfo("And", DataRepositoryMethodKeywordType.COMBINE_CONDITIONS),
			new QueryPredicateKeywordInfo("Or", DataRepositoryMethodKeywordType.COMBINE_CONDITIONS),
			new QueryPredicateKeywordInfo("After", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsAfter", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Before", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsBefore", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Containing", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsContaining", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Contains", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Between", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsBetween", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("EndingWith", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsEndingWith", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("EndsWith", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Exists", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("False", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("IsFalse", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("GreaterThan", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsGreaterThan", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("GreaterThanEqual", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsGreaterThanEqual", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("In", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsIn", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Is", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Equals", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Empty", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("IsEmpty", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("NotEmpty", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("IsNotEmpty", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("NotNull", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("IsNotNull", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("Null", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("IsNull", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("LessThan", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsLessThan", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("LessThanEqual", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsLessThanEqual", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Like", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsLike", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Near", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsNear", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Not", DataRepositoryMethodKeywordType.IGNORE),
			new QueryPredicateKeywordInfo("IsNot", DataRepositoryMethodKeywordType.IGNORE),
			new QueryPredicateKeywordInfo("NotIn", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsNotIn", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("NotLike", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsNotLike", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Regex", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("MatchesRegex", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("Matches", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("StartingWith", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsStartingWith", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("StartsWith", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("True", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("IsTrue", DataRepositoryMethodKeywordType.TERMINATE_EXPRESSION),
			new QueryPredicateKeywordInfo("Within", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IsWithin", DataRepositoryMethodKeywordType.COMPARE),
			new QueryPredicateKeywordInfo("IgnoreCase", DataRepositoryMethodKeywordType.IGNORE),
			new QueryPredicateKeywordInfo("IgnoringCase", DataRepositoryMethodKeywordType.IGNORE),
			new QueryPredicateKeywordInfo("AllIgnoreCase", DataRepositoryMethodKeywordType.IGNORE),
			new QueryPredicateKeywordInfo("AllIgnoringCase", DataRepositoryMethodKeywordType.IGNORE),
			new QueryPredicateKeywordInfo("OrderBy", DataRepositoryMethodKeywordType.COMBINE_CONDITIONS)
			);
}