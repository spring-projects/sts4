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
package org.springframework.ide.vscode.boot.java.data.jpa.queries;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.spel.SpelSemanticTokens;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;

public class MySqlSemanticTokensTest {
	
	private MySqlSemanticTokens provider;
	
	@BeforeEach
	void setup() {
		provider = new MySqlSemanticTokens(Optional.of(new SpelSemanticTokens()), Optional.of(Assertions::fail));
	}
	
	@Test
	void simple() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT * from Document document WHERE document.id=fn_module_candidates()", 0);
		assertThat(tokens.size()).isEqualTo(13);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0])); // from
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 22, "variable", new String[0])); // Document
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(23, 31, "variable", new String[0])); // document
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(32, 37, "keyword", new String[0])); // WHERE
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(38, 46, "variable", new String[0])); // document
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(46, 47, "operator", new String[0])); // .
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(47, 49, "property", new String[0])); // id
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(49, 50, "operator", new String[0])); // =
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(50, 70, "method", new String[0])); // fn_module_candidates
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(70, 71, "operator", new String[0])); // (
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(71, 72, "operator", new String[0])); // )
		
	}
	
	@Test
	void parametersInQuery() {
		List<SemanticTokenData> tokens = provider.computeTokens("DELETE FROM component_document WHERE item_document_id = :itemDocumentId", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 11, "keyword", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(12, 30, "variable", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(31, 36, "keyword", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(37, 53, "variable", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(54, 55, "operator", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(56, 57, "operator", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(57, 71, "parameter", new String[0]));
		
		assertThat(tokens.size()).isEqualTo(8);
	}
	
	@Test
	void spelInQuery() {
		List<SemanticTokenData> tokens = provider.computeTokens("DELETE FROM component_document WHERE item_document_id = :#{someBean}", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 11, "keyword", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(12, 30, "variable", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(31, 36, "keyword", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(37, 53, "variable", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(54, 55, "operator", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(56, 57, "operator", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(57, 59, "operator", new String[0]));
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(59, 67, "variable", new String[0]));
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(67, 68, "operator", new String[0]));
		
		assertThat(tokens.size()).isEqualTo(10);
	}
	
	@Test
	void complexSpelInQuery() {
		List<SemanticTokenData> tokens = provider.computeTokens("DELETE FROM component_document WHERE item_document_id = :#{someBean.someProperty != null ? someBean.someProperty : 'default'}", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 11, "keyword", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(12, 30, "variable", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(31, 36, "keyword", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(37, 53, "variable", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(54, 55, "operator", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(56, 57, "operator", new String[0])); // :
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(57, 59, "operator", new String[0])); // #{
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(59, 67, "variable", new String[0])); // someBean
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(67, 68, "operator", new String[0])); // .
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(68, 80, "property", new String[0])); // someProperty
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(81, 83, "operator", new String[0])); // != 
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(84, 88, "keyword", new String[0])); // null
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(89, 90, "operator", new String[0])); // ?
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(91, 99, "variable", new String[0])); // someBean
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(99, 100, "operator", new String[0])); // .
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(100, 112, "property", new String[0])); // someProperty
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(113, 114, "operator", new String[0])); // :
		assertThat(tokens.get(18)).isEqualTo(new SemanticTokenData(115, 124, "string", new String[0])); // 'default'
		assertThat(tokens.get(19)).isEqualTo(new SemanticTokenData(124, 125, "operator", new String[0])); // }
		
		assertThat(tokens.size()).isEqualTo(20);
	}

}
