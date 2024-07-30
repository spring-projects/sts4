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
	
	@Test
	void semiColonAtEnd() {
		List<SemanticTokenData> tokens = provider.computeTokens(" select count(*) from anecdote where anecdote_id=:anecdote ; ", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(1, 7, "keyword", new String[0])); // select
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(8, 13, "keyword", new String[0])); // count
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(13, 14, "operator", new String[0])); // (
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 15, "operator", new String[0])); // *
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(15, 16, "operator", new String[0])); // )
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(17, 21, "keyword", new String[0])); // from 
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(22, 30, "variable", new String[0])); // anecdote
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(31, 36, "keyword", new String[0])); // where
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(37, 48, "variable", new String[0])); // anecdote_id
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(48, 49, "operator", new String[0])); // =
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(49, 50, "operator", new String[0])); // :
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(50, 58, "parameter", new String[0])); // anecdote
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(59, 60, "operator", new String[0])); // ;
		
		assertThat(tokens.size()).isEqualTo(13);
	}
	
	@Test
	void parameterInLimitClause_1() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT * FROM cards ORDER BY random() LIMIT :2", 0);
		assertThat(tokens.size()).isEqualTo(12);
		
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 19, "variable", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(20, 25, "keyword", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(26, 28, "keyword", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(29, 35, "method", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(35, 36, "operator", new String[0]));
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(36, 37, "operator", new String[0]));
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(38, 43, "keyword", new String[0]));
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(44, 45, "operator", new String[0]));
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(45, 46, "parameter", new String[0]));
	}

	@Test
	void parameterInLimitClause_2() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT * FROM cards ORDER BY random() LIMIT ?2", 0);
		assertThat(tokens.size()).isEqualTo(12);
		
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 19, "variable", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(20, 25, "keyword", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(26, 28, "keyword", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(29, 35, "method", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(35, 36, "operator", new String[0]));
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(36, 37, "operator", new String[0]));
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(38, 43, "keyword", new String[0]));
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(44, 45, "operator", new String[0]));
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(45, 46, "parameter", new String[0]));
	}
	
	@Test
	void parameterInLimitClause_3() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT * FROM cards ORDER BY random() LIMIT :#{qq}", 0);
		assertThat(tokens.size()).isEqualTo(14);
		
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 19, "variable", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(20, 25, "keyword", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(26, 28, "keyword", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(29, 35, "method", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(35, 36, "operator", new String[0]));
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(36, 37, "operator", new String[0]));
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(38, 43, "keyword", new String[0]));
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(44, 45, "operator", new String[0]));
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(45, 47, "operator", new String[0]));
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(47, 49, "variable", new String[0]));
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(49, 50, "operator", new String[0]));
	}

	@Test
	void parameterInLimitClause_4() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT * FROM cards ORDER BY random() LIMIT :limit", 0);
		assertThat(tokens.size()).isEqualTo(12);
		
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 19, "variable", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(20, 25, "keyword", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(26, 28, "keyword", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(29, 35, "method", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(35, 36, "operator", new String[0]));
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(36, 37, "operator", new String[0]));
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(38, 43, "keyword", new String[0]));
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(44, 45, "operator", new String[0]));
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(45, 50, "parameter", new String[0]));
	}
}
