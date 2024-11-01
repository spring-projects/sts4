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
package org.springframework.ide.vscode.boot.java.cron;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;

public class CronSemanticTokensTest {
	
	private CronSemanticTokens provider;
	
	@BeforeEach
	void setup() {
		provider = new CronSemanticTokens(Optional.of(Assertions::fail));
	}
	
	@Test
	void topHoueEveryDayEveryWeek() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 * * * *");
		assertThat(tokens.size()).isEqualTo(6);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "operator", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "operator", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(8, 9, "operator", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(10, 11, "operator", new String[0]));
	}
	
	@Test
	void everyTenSeconds() {
		List<SemanticTokenData> tokens = provider.computeTokens("*/10 * * * * *");
		assertThat(tokens.size()).isEqualTo(8);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "operator", new String[0]));
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(1, 2, "operator", new String[0]));
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(2, 4, "number", new String[0]));
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(5, 6, "operator", new String[0]));
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0]));
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(9, 10, "operator", new String[0]));
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0]));
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(13, 14, "operator", new String[0]));
	}

	@Test
	void betweenEightAndTenEveryDay() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 8-10 * * *");
		assertThat(tokens.size()).isEqualTo(8);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 8
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(5, 6, "operator", new String[0])); // -
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(6, 8, "number", new String[0])); // 10
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(9, 10, "operator", new String[0])); // *
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0])); // *
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(13, 14, "operator", new String[0])); // *
	}
	
	@Test
	void everyDayBetweenSixAndSeven() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 6,19 * * *");
		assertThat(tokens.size()).isEqualTo(8);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 6
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(5, 6, "operator", new String[0])); // ,
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(6, 8, "number", new String[0])); // 19
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(9, 10, "operator", new String[0])); // *
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0])); // *
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(13, 14, "operator", new String[0])); // *
	}
	
	@Test
	void everyHalfHourBetweenEightAndEleven() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0/30 8-10 * * *");
		assertThat(tokens.size()).isEqualTo(10);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(3, 4, "operator", new String[0])); // /
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(4, 6, "number", new String[0])); // 30
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(7, 8, "number", new String[0])); // 8
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(8, 9, "operator", new String[0])); // -
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(9, 11, "number", new String[0])); // 10
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(12, 13, "operator", new String[0])); // *
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(14, 15, "operator", new String[0])); // *
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(16, 17, "operator", new String[0])); // *
	}

	@Test
	void nineToFiveOnWeekdays() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 9-17 * * MON-FRI");
		assertThat(tokens.size()).isEqualTo(10);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 9
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(5, 6, "operator", new String[0])); // -
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(6, 8, "number", new String[0])); // 17
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(9, 10, "operator", new String[0])); // *
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0])); // *
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(13, 16, "enum", new String[0])); // MON
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(16, 17, "operator", new String[0])); // -
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(17, 20, "enum", new String[0])); // FRI
	}

	@Test
	void christmas() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 25 12 ?");
		assertThat(tokens.size()).isEqualTo(6);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 8, "number", new String[0])); // 25
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(9, 11, "number", new String[0])); // 12
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(12, 13, "operator", new String[0])); // ?
	}

	@Test
	void lastDayOfMonthAtMidnight() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 L * *");
		assertThat(tokens.size()).isEqualTo(6);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "method", new String[0])); // L
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(8, 9, "operator", new String[0])); // *
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(10, 11, "operator", new String[0])); // *
	}

	@Test
	void thirdToLasttDayOfMonthAtMidnight() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 L-3 * *");
		assertThat(tokens.size()).isEqualTo(8);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "method", new String[0])); // L
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(7, 8, "operator", new String[0])); // -
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(8, 9, "number", new String[0])); // 3
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(10, 11, "operator", new String[0])); // *
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(12, 13, "operator", new String[0])); // *
	}

	@Test
	void firstWeekDayOfMonthAtMidnight() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 1W * *");
		assertThat(tokens.size()).isEqualTo(7);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "number", new String[0])); // 1
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(7, 8, "method", new String[0])); // W
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(9, 10, "operator", new String[0])); // *
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0])); // *
	}

	@Test
	void lastWeekDayOfMonthAtMidnight() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 LW * *");
		assertThat(tokens.size()).isEqualTo(6);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 8, "method", new String[0])); // LW
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(9, 10, "operator", new String[0])); // *
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0])); // *
	}

	@Test
	void lastFridayOfMonthAtMidnight() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 * * 5L");
		assertThat(tokens.size()).isEqualTo(7);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "operator", new String[0])); // *
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(8, 9, "operator", new String[0])); // *
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(10, 11, "number", new String[0])); // 5
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(11, 12, "method", new String[0])); // L
	}

	@Test
	void lastThursdayOfMonthAtMidnight() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 * * THUL");
		assertThat(tokens.size()).isEqualTo(7);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "operator", new String[0])); // *
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(8, 9, "operator", new String[0])); // *
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(10, 13, "enum", new String[0])); // THU
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(13, 14, "method", new String[0])); // L
	}

	@Test
	void secondFridayOfMonthAtMidnight() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 ? * 5#2");
		assertThat(tokens.size()).isEqualTo(8);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "operator", new String[0])); // ?
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(8, 9, "operator", new String[0])); // *
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(10, 11, "number", new String[0])); // 5
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(11, 12, "operator", new String[0])); // #
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(12, 13, "number", new String[0])); // 2
	}

	@Test
	void firstMondayOfMonthAtMidnight() {
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 ? * MON#1");
		assertThat(tokens.size()).isEqualTo(8);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "operator", new String[0])); // ?
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(8, 9, "operator", new String[0])); // *
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(10, 13, "enum", new String[0])); // MON
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(13, 14, "operator", new String[0])); // #
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(14, 15, "number", new String[0])); // 1
	}
	
	@Test
	void macro() {
		List<SemanticTokenData> tokens = provider.computeTokens("@yearly");
		assertThat(tokens.size()).isEqualTo(1);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 7, "macro", new String[0])); // @yearly
	}
	
	@Test
	void errors_1() {
		provider = new CronSemanticTokens();
		List<SemanticTokenData> tokens = provider.computeTokens("0 0 0 8LW * Foo#bar");
		assertThat(tokens.size()).isEqualTo(9);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 1, "number", new String[0])); // 0
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "number", new String[0])); // 0
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(4, 5, "number", new String[0])); // 0
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(6, 7, "number", new String[0])); // 8
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(7, 9, "method", new String[0])); // LW
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(10, 11, "operator", new String[0])); // *
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(12, 15, "enum", new String[0])); // Foo
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(15, 16, "operator", new String[0])); // #
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(16, 19, "enum", new String[0])); // bar
	}

	@Test
	void errors_2() {
		provider = new CronSemanticTokens();
		List<SemanticTokenData> tokens = provider.computeTokens("qq#3 0 Blah 1-88LW * JUL-MARCH");
		assertThat(tokens.size()).isEqualTo(13);
		assertThat(tokens.get(0)).isEqualTo(new SemanticTokenData(0, 2, "enum", new String[0])); // qq
		assertThat(tokens.get(1)).isEqualTo(new SemanticTokenData(2, 3, "operator", new String[0])); // #
		assertThat(tokens.get(2)).isEqualTo(new SemanticTokenData(3, 4, "number", new String[0])); // 3
		assertThat(tokens.get(3)).isEqualTo(new SemanticTokenData(5, 6, "number", new String[0])); // 0
		assertThat(tokens.get(4)).isEqualTo(new SemanticTokenData(7, 11, "enum", new String[0])); // Blah
		assertThat(tokens.get(5)).isEqualTo(new SemanticTokenData(12, 13, "number", new String[0])); // 1
		assertThat(tokens.get(6)).isEqualTo(new SemanticTokenData(13, 14, "operator", new String[0])); // -
		assertThat(tokens.get(7)).isEqualTo(new SemanticTokenData(14, 16, "number", new String[0])); // 88
		assertThat(tokens.get(8)).isEqualTo(new SemanticTokenData(16, 18, "method", new String[0])); // LW
		assertThat(tokens.get(9)).isEqualTo(new SemanticTokenData(19, 20, "operator", new String[0])); // *
		assertThat(tokens.get(10)).isEqualTo(new SemanticTokenData(21, 24, "enum", new String[0])); // JUL
		assertThat(tokens.get(11)).isEqualTo(new SemanticTokenData(24, 25, "operator", new String[0])); // -
		assertThat(tokens.get(12)).isEqualTo(new SemanticTokenData(25, 30, "enum", new String[0])); // MARCH
	}
	
}
