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

public class JpqlSemanticTokensTest {
	
	private JpqlSemanticTokens provider;
	
	@BeforeEach
	void setup() {
		provider = new JpqlSemanticTokens(Optional.empty(), Optional.of(Assertions::fail));
	}
	
	@Test
	void simpleQuery_1() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT owner FROM Owner owner", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 12, "variable", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(13, 17, "keyword", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(18, 23, "class", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(24, 29, "variable", new String[0]));
		
		assertThat(tokens.size()).isEqualTo(5);
	}

	@Test
	void initialOffset() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT owner FROM Owner owner", 3);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(3, 9, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(10, 15, "variable", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(16, 20, "keyword", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(21, 26, "class", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(27, 32, "variable", new String[0]));
		
		assertThat(tokens.size()).isEqualTo(5);
	}
	
	@Test
	void query_with_conflicting_groupby() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT g FROM G g GROUP BY g.name", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0])); // SELECT
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "variable", new String[0])); // g
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0])); // FROM
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 15, "class", new String[0])); //G
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(16, 17, "variable", new String[0])); // g
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(18, 23, "keyword", new String[0])); // GROUP
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(24, 26, "keyword", new String[0])); // BY
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(27, 28, "variable", new String[0])); // g
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(28, 29, "operator", new String[0])); // .
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(29, 33, "method", new String[0])); // name
		
		assertThat(tokens.size()).isEqualTo(10);
	}
	
	@Test
	void query_with_parameter() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT f from Student f LEFT JOIN f.classTbls s WHERE s.ClassName = :className", 0);
		
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0])); // SELECT
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "variable", new String[0])); // f
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0])); // from
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 21, "class", new String[0])); // Student
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(22, 23, "variable", new String[0])); // f
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(24, 28, "keyword", new String[0])); // LEFT
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(29, 33, "keyword", new String[0])); // JOIN
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(34, 35, "variable", new String[0])); // f
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(35, 36, "operator", new String[0])); // .
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(36, 45, "method", new String[0])); // classTbls
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(46, 47, "variable", new String[0])); // s
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(48, 53, "keyword", new String[0])); // WHERE
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(54, 55, "variable", new String[0])); // s
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(55, 56, "operator", new String[0])); // .
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(56, 65, "method", new String[0])); // ClassName
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(66, 67, "operator", new String[0])); // =
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(68, 69, "operator", new String[0])); // :
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(69, 78, "parameter", new String[0])); // className
		
		assertThat(tokens.size()).isEqualTo(18);
	}

	@Test
	void query_with_SPEL() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:#{id}", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0])); // SELECT
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 12, "variable", new String[0])); // owner
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(13, 17, "keyword", new String[0])); // FROM
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(18, 23, "class", new String[0])); // Owner
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(24, 29, "variable", new String[0])); // owner
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(30, 34, "keyword", new String[0])); // left
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(35, 39, "keyword", new String[0])); // join
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(40, 45, "keyword", new String[0])); // fetch
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(46, 51, "variable", new String[0])); // owner
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(51, 52, "operator", new String[0])); // .
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(52, 56, "method", new String[0])); // pets
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(57, 62, "keyword", new String[0])); // WHERE
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(63, 68, "variable", new String[0])); // owner
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(68, 69, "operator", new String[0])); // .
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(69, 71, "method", new String[0])); // id
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(72, 73, "operator", new String[0])); // =
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(73, 74, "operator", new String[0])); // :
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(74, 76, "operator", new String[0])); // #{
		assertThat(tokens.get(18)).isEqualTo(new SemanticTokenData(76, 78, "string", new String[0])); // id
		assertThat(tokens.get(19)).isEqualTo(new SemanticTokenData(78, 79, "operator", new String[0])); // }
		
		assertThat(tokens.size()).isEqualTo(20);
	}
	
	@Test
	void query_with_SPEL_Tokens() {
		provider = new JpqlSemanticTokens(Optional.of(new SpelSemanticTokens()));
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:#{id}", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0])); // SELECT
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 12, "variable", new String[0])); // owner
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(13, 17, "keyword", new String[0])); // FROM
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(18, 23, "class", new String[0])); // Owner
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(24, 29, "variable", new String[0])); // owner
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(30, 34, "keyword", new String[0])); // left
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(35, 39, "keyword", new String[0])); // join
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(40, 45, "keyword", new String[0])); // fetch
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(46, 51, "variable", new String[0])); // owner
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(51, 52, "operator", new String[0])); // .
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(52, 56, "method", new String[0])); // pets
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(57, 62, "keyword", new String[0])); // WHERE
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(63, 68, "variable", new String[0])); // owner
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(68, 69, "operator", new String[0])); // .
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(69, 71, "method", new String[0])); // id
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(72, 73, "operator", new String[0])); // =
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(73, 74, "operator", new String[0])); // :
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(74, 76, "operator", new String[0])); // #{
		assertThat(tokens.get(18)).isEqualTo(new SemanticTokenData(76, 78, "variable", new String[0])); // id
		assertThat(tokens.get(19)).isEqualTo(new SemanticTokenData(78, 79, "operator", new String[0])); // }
		
		assertThat(tokens.size()).isEqualTo(20);
	}

}
