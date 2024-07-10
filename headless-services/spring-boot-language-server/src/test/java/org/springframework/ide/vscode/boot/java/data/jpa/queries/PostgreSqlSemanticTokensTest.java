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

public class PostgreSqlSemanticTokensTest {

	private PostgreSqlSemanticTokens provider;
	
	@BeforeEach
	void setup() {
		provider = new PostgreSqlSemanticTokens(Optional.of(new SpelSemanticTokens()), Optional.of(Assertions::fail));
	}
	
	@Test
	void simple() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT * from fn_module_candidates", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 34, "variable", new String[0]));
		
		assertThat(tokens.size()).isEqualTo(4);
	}
	
	@Test
	void adv_1() {
		List<SemanticTokenData> tokens = provider.computeTokens("""
				DELETE FROM Represenation representation
				WHERE representation.project_id=?1 
				AND NOT EXISTS (
					SELECT * FROM Document document 
					WHERE document.project_id=?1 
					AND json_path_exists(document.content::jsonb, ('strict $.content.**.id ? (@ == "\\' || representation.targetobjectid || \\'")')::jsonpath)
				)
				""", 0);
		assertThat(tokens.size()).isEqualTo(43);
		
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(74, 75, "parameter", new String[0])); // 1 from ?1
		assertThat(tokens.get(26)).isEqualTo(new SemanticTokenData(154, 155, "parameter", new String[0])); // 1 from ?1
		assertThat(tokens.get(28)).isEqualTo(new SemanticTokenData(161, 177, "method", new String[0])); // json_path_exists
		assertThat(tokens.get(33)).isEqualTo(new SemanticTokenData(194, 196, "operator", new String[0])); // ::
		assertThat(tokens.get(34)).isEqualTo(new SemanticTokenData(196, 201, "type", new String[0])); // ::
		assertThat(tokens.get(37)).isEqualTo(new SemanticTokenData(204, 281, "string", new String[0])); // 'strict $.content.**.id ? (@ == "\\' || representation.targetobjectid || \\'")'
		assertThat(tokens.get(39)).isEqualTo(new SemanticTokenData(282, 284, "operator", new String[0])); // ::
		assertThat(tokens.get(40)).isEqualTo(new SemanticTokenData(284, 292, "type", new String[0])); // ::
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

}
