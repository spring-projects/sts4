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

public class HqlSemanticTokensTest {
	
	private HqlSemanticTokens provider;
	
	@BeforeEach
	void setup() {
		provider = new HqlSemanticTokens(Optional.empty(), Optional.of(Assertions::fail));
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
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT g FROM Group g GROUP BY g.name", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 8, "variable", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(9, 13, "keyword", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(14, 19, "class", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(20, 21, "variable", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(22, 27, "keyword", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(28, 30, "keyword", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(31, 32, "variable", new String[0]));
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(32, 33, "operator", new String[0]));
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(33, 37, "property", new String[0]));
		
		assertThat(tokens.size()).isEqualTo(10);
	}
	
	@Test
	void query_with_parameter() {
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT DISTINCT owner FROM Owner owner left join  owner.pets WHERE owner.lastName LIKE :lastName%", 0);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0])); // SELECT
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 15, "keyword", new String[0])); // DISTINCT
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(16, 21, "variable", new String[0])); // owner
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(22, 26, "keyword", new String[0])); // FROM
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(27, 32, "class", new String[0])); // Owner
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(33, 38, "variable", new String[0])); // owner
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(39, 43, "keyword", new String[0])); // left
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(44, 48, "keyword", new String[0])); // join
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(50, 55, "variable", new String[0])); // owner
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(55, 56, "operator", new String[0])); // .
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(56, 60, "property", new String[0])); // pets
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(61, 66, "keyword", new String[0])); // WHERE
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(67, 72, "variable", new String[0])); // owner
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(72, 73, "operator", new String[0])); // .
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(73, 81, "property", new String[0])); // lastName
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(82, 86, "keyword", new String[0])); // LIKE
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(87, 88, "operator", new String[0])); // :
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(88, 96, "parameter", new String[0])); // lastName
		assertThat(tokens.get(18)).isEqualTo(new SemanticTokenData(96, 97, "operator", new String[0])); // lastName
		
		assertThat(tokens.size()).isEqualTo(19);
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
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(52, 56, "property", new String[0])); // pets
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(57, 62, "keyword", new String[0])); // WHERE
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(63, 68, "variable", new String[0])); // owner
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(68, 69, "operator", new String[0])); // .
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(69, 71, "property", new String[0])); // id
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(72, 73, "operator", new String[0])); // =
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(73, 74, "operator", new String[0])); // :
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(74, 76, "operator", new String[0])); // #{
		assertThat(tokens.get(18)).isEqualTo(new SemanticTokenData(76, 78, "string", new String[0])); // id
		assertThat(tokens.get(19)).isEqualTo(new SemanticTokenData(78, 79, "operator", new String[0])); // }
		
		assertThat(tokens.size()).isEqualTo(20);
	}

	@Test
	void query_with_SPEL_Tokens() {
		provider = new HqlSemanticTokens(Optional.of(new SpelSemanticTokens()));
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
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(52, 56, "property", new String[0])); // pets
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(57, 62, "keyword", new String[0])); // WHERE
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(63, 68, "variable", new String[0])); // owner
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(68, 69, "operator", new String[0])); // .
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(69, 71, "property", new String[0])); // id
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(72, 73, "operator", new String[0])); // =
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(73, 74, "operator", new String[0])); // :
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(74, 76, "operator", new String[0])); // #{
		assertThat(tokens.get(18)).isEqualTo(new SemanticTokenData(76, 78, "variable", new String[0])); // id
		assertThat(tokens.get(19)).isEqualTo(new SemanticTokenData(78, 79, "operator", new String[0])); // }
		
		assertThat(tokens.size()).isEqualTo(20);
	}

	@Test
	void query_with_complex_SPEL_Tokens() {
		provider = new HqlSemanticTokens(Optional.of(new SpelSemanticTokens()));
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:#{someBean.someProperty != null ? someBean.someProperty : 'default'}", 0);
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
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(52, 56, "property", new String[0])); // pets
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(57, 62, "keyword", new String[0])); // WHERE
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(63, 68, "variable", new String[0])); // owner
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(68, 69, "operator", new String[0])); // .
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(69, 71, "property", new String[0])); // id
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(72, 73, "operator", new String[0])); // =
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(73, 74, "operator", new String[0])); // :
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(74, 76, "operator", new String[0])); // #{
		assertThat(tokens.get(18)).isEqualTo(new SemanticTokenData(76, 84, "variable", new String[0])); // someBean
		assertThat(tokens.get(19)).isEqualTo(new SemanticTokenData(84, 85, "operator", new String[0])); // .
		assertThat(tokens.get(20)).isEqualTo(new SemanticTokenData(85, 97, "property", new String[0])); // someProperty
		assertThat(tokens.get(21)).isEqualTo(new SemanticTokenData(98, 100, "operator", new String[0])); // !=
		assertThat(tokens.get(22)).isEqualTo(new SemanticTokenData(101, 105, "keyword", new String[0])); // null
		assertThat(tokens.get(23)).isEqualTo(new SemanticTokenData(106, 107, "operator", new String[0])); // ?
		assertThat(tokens.get(24)).isEqualTo(new SemanticTokenData(108, 116, "variable", new String[0])); // someBean
		assertThat(tokens.get(25)).isEqualTo(new SemanticTokenData(116, 117, "operator", new String[0])); // .
		assertThat(tokens.get(26)).isEqualTo(new SemanticTokenData(117, 129, "property", new String[0])); // someProperty
		assertThat(tokens.get(27)).isEqualTo(new SemanticTokenData(130, 131, "operator", new String[0])); // :
		assertThat(tokens.get(28)).isEqualTo(new SemanticTokenData(132, 141, "string", new String[0])); // 'default'
		assertThat(tokens.get(29)).isEqualTo(new SemanticTokenData(141, 142, "operator", new String[0])); // }
		
		assertThat(tokens.size()).isEqualTo(30);
	}
	
	@Test
	void instatiotation_1() {
		provider = new HqlSemanticTokens(Optional.of(new SpelSemanticTokens()));
		List<SemanticTokenData> tokens = provider.computeTokens("SELECT new com.example.ls.issue.SampleTableSizePojo(t.schemaName, sum(t.tableSize) ) FROM MTables t GROUP BY t.schemaName ORDER BY sum(t.tableSize) DESC", 0);
		assertThat(tokens.size()).isEqualTo(32);
		
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 6, "keyword", new String[0])); // SELECT
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(7, 10, "keyword", new String[0])); // new
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(11, 51, "method", new String[0])); // com.example.ls.issue.SampleTableSizePojo
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(51, 52, "operator", new String[0])); // (
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(52, 53, "variable", new String[0])); // t
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(53, 54, "operator", new String[0])); // .
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(54, 64, "property", new String[0])); // schemaName
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(64, 65, "operator", new String[0])); // ,
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(66, 69, "keyword", new String[0])); // sum
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(69, 70, "operator", new String[0])); // (
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(70, 71, "variable", new String[0])); // t
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(71, 72, "operator", new String[0])); // .
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(72, 81, "property", new String[0])); // tableSize
		assertThat(tokens.get(13)).isEqualTo(new SemanticTokenData(81, 82, "operator", new String[0])); // )
		assertThat(tokens.get(14)).isEqualTo(new SemanticTokenData(83, 84, "operator", new String[0])); // )
		assertThat(tokens.get(15)).isEqualTo(new SemanticTokenData(85, 89, "keyword", new String[0])); // FROM
		assertThat(tokens.get(16)).isEqualTo(new SemanticTokenData(90, 97, "class", new String[0])); // MTables
		assertThat(tokens.get(17)).isEqualTo(new SemanticTokenData(98, 99, "variable", new String[0])); // t
		assertThat(tokens.get(18)).isEqualTo(new SemanticTokenData(100, 105, "keyword", new String[0])); // GROUP
		assertThat(tokens.get(19)).isEqualTo(new SemanticTokenData(106, 108, "keyword", new String[0])); // BY
		assertThat(tokens.get(20)).isEqualTo(new SemanticTokenData(109, 110, "variable", new String[0])); // t
		assertThat(tokens.get(21)).isEqualTo(new SemanticTokenData(110, 111, "operator", new String[0])); // .
		assertThat(tokens.get(22)).isEqualTo(new SemanticTokenData(111, 121, "property", new String[0])); // schemaName
		assertThat(tokens.get(23)).isEqualTo(new SemanticTokenData(122, 127, "keyword", new String[0])); // ORDER
		assertThat(tokens.get(24)).isEqualTo(new SemanticTokenData(128, 130, "keyword", new String[0])); // BY
		assertThat(tokens.get(25)).isEqualTo(new SemanticTokenData(131, 134, "keyword", new String[0])); // sum
		assertThat(tokens.get(26)).isEqualTo(new SemanticTokenData(134, 135, "operator", new String[0])); // (
		assertThat(tokens.get(27)).isEqualTo(new SemanticTokenData(135, 136, "variable", new String[0])); // t
		assertThat(tokens.get(28)).isEqualTo(new SemanticTokenData(136, 137, "operator", new String[0])); // .
		assertThat(tokens.get(29)).isEqualTo(new SemanticTokenData(137, 146, "property", new String[0])); // tableSize
		assertThat(tokens.get(30)).isEqualTo(new SemanticTokenData(146, 147, "operator", new String[0])); // )
		assertThat(tokens.get(31)).isEqualTo(new SemanticTokenData(148, 152, "keyword", new String[0])); // DESC
	}
}
