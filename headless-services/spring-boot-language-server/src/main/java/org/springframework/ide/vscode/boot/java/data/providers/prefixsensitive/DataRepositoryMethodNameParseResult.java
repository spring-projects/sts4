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
import java.util.Set;

import org.springframework.ide.vscode.boot.java.data.providers.QueryMethodSubject;

/**
 * Represents the result of parsing a Spring Data repository query method
 * @author danthe1st
 */
record DataRepositoryMethodNameParseResult(
		/**
		 * Information about the subject of the method
		 */
		QueryMethodSubject subjectType,
		/**
		 * parameters required for calling the method
		 */
		List<String> parameters,
		/**
		 * {@code true} if the whole method shall be replaced including parameters, else false
		 */
		boolean performFullCompletion,
		/**
		 * the last entered word, which completion options should be used for completing the expression.
		 *
		 * e.g. {@code First} in {@code findByFirst} which could be completed to {@code findByFirstName}
		 */
		String lastWord,
		/**
		 * types of keywords that can be completed with
		 */
		Set<DataRepositoryMethodKeywordType> allowedKeywordTypes) {

}