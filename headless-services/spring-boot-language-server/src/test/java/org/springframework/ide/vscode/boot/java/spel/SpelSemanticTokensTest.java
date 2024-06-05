/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.spel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;

public class SpelSemanticTokensTest {

	private SpelSemanticTokens provider = new SpelSemanticTokens();
	
	@Test
	void simpleCompare() {
		List<SemanticTokenData> tokens = provider.computeTokens("1 >= 1", 0);
		assertThat(tokens.size()).isEqualTo(3);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 4, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(5, 6, "number", new String[0]));
	}
	
	@Test
	void simpleCompareWithOp() {
		List<SemanticTokenData> tokens = provider.computeTokens("1 ge 1", 0);
		assertThat(tokens.size()).isEqualTo(3);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 4, "keyword", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(5, 6, "number", new String[0]));
	}
	
	@Test
	void logicalOperators() {
		List<SemanticTokenData> tokens = provider.computeTokens("4 > 3 or 15 < 10", 0);
		assertThat(tokens.size()).isEqualTo(7);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 8, "keyword", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(9, 11, "number", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(12, 13, "operator", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(14, 16, "number", new String[0]));
	}
	
	@Test
	void conditionalOperators() {
		List<SemanticTokenData> tokens = provider.computeTokens("someBean.someProperty != null ? someBean.someProperty : 'default'", 0);
		assertThat(tokens.size()).isEqualTo(11);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 8, "variable", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(8, 9, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 21, "property", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(22, 24, "operator", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(25, 29, "keyword", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(30, 31, "operator", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(32, 40, "variable", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(40, 41, "operator", new String[0]));
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(41, 53, "property", new String[0]));
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(54, 55, "operator", new String[0]));
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(56, 65, "string", new String[0]));
	}

	@Test
	void regex() {
		List<SemanticTokenData> tokens = provider.computeTokens("'invalid alphabetic string #$1' matches '[a-zA-Z\\s]+' ", 0);
		assertThat(tokens.size()).isEqualTo(3);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 31, "string", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(32, 39, "keyword", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(40, 53, "string", new String[0]));
	}
	
	@Test
	void accessMapObj() {
		List<SemanticTokenData> tokens = provider.computeTokens("carPark.carsByDriver['Driver1']", 0);
		assertThat(tokens.size()).isEqualTo(6);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 7, "variable", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(8, 20, "property", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(20, 21, "operator", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(21, 30, "string", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(30, 31, "operator", new String[0]));
	}

	@Test
	void accessListItems() {
		List<SemanticTokenData> tokens = provider.computeTokens("carPark.cars[0]", 0);
		assertThat(tokens.size()).isEqualTo(6);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 7, "variable", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(8, 12, "property", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(12, 13, "operator", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(13, 14, "number", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(14, 15, "operator", new String[0]));
	}

	@Test
	void beanReference() {
		List<SemanticTokenData> tokens = provider.computeTokens("@vetRepo", 0);
		assertThat(tokens.size()).isEqualTo(2);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "operator", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(1, 8, "type", new String[0]));
	}

	@Test
	void contructorAndMethod() {
		List<SemanticTokenData> tokens = provider.computeTokens("new String('hello world').toUpperCase()", 0);
		assertThat(tokens.size()).isEqualTo(9);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 3, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(4, 10, "method", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(10, 11, "operator", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(11, 24, "string", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(24, 25, "operator", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(25, 26, "operator", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(26, 37, "method", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(37, 38, "operator", new String[0]));
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(38, 39, "operator", new String[0]));
	}

}
