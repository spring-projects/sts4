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

import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.boot.java.reconcilers.CompositeASTVisitor;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class JdtCronSemanticTokensProviderTest {

	@Autowired
	JdtCronSemanticTokensProvider provider;

	private ProjectsHarness projects = ProjectsHarness.INSTANCE;

	private MavenJavaProject jp;

	@BeforeEach
	public void setup() throws Exception {
		jp = projects.mavenProject("spring-modulith-example-full");
	}

	private List<SemanticTokenData> computeTokens(CompilationUnit cu) {
		Collector<SemanticTokenData> collector = new Collector<>();
		CompositeASTVisitor visitor = new CompositeASTVisitor();
		visitor.add(provider.getTokensComputer(jp, null, cu, collector));
		cu.accept(visitor);
		return collector.get();
	}

	@Test
	void secondFridayOfMonthAtMidnight() throws Exception {
		String source = """
				package my.package

				import org.springframework.scheduling.annotation.Scheduled;

				public class A {

					@Scheduled(cron="0 0 0 ? * 5#2")
					void foo() {}

				}
				""";

		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri()
				.toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "A.java", jp);

		assertThat(cu).isNotNull();

		List<SemanticTokenData> tokens = computeTokens(cu);

		assertThat(tokens.size()).isEqualTo(8);

		SemanticTokenData token = tokens.get(0);
		assertThat(token).isEqualTo(new SemanticTokenData(117, 118, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(1);
		assertThat(token).isEqualTo(new SemanticTokenData(119, 120, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(2);
		assertThat(token).isEqualTo(new SemanticTokenData(121, 122, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(3);
		assertThat(token).isEqualTo(new SemanticTokenData(123, 124, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("?");

		token = tokens.get(4);
		assertThat(token).isEqualTo(new SemanticTokenData(125, 126, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("*");

		token = tokens.get(5);
		assertThat(token).isEqualTo(new SemanticTokenData(127, 128, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("5");

		token = tokens.get(6);
		assertThat(token).isEqualTo(new SemanticTokenData(128, 129, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("#");

		token = tokens.get(7);
		assertThat(token).isEqualTo(new SemanticTokenData(129, 130, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("2");

	}

	@Test
	void lastThursdayOfMonthAtMidnight() throws Exception {
		String source = """
				package my.package

				import org.springframework.scheduling.annotation.Scheduled;

				public class A {

					@Scheduled(cron="0 0 0 * * THUL")
					void foo() {}

				}
				""";

		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri()
				.toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "A.java", jp);

		assertThat(cu).isNotNull();

		List<SemanticTokenData> tokens = computeTokens(cu);

		assertThat(tokens.size()).isEqualTo(7);

		SemanticTokenData token = tokens.get(0);
		assertThat(token).isEqualTo(new SemanticTokenData(117, 118, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(1);
		assertThat(token).isEqualTo(new SemanticTokenData(119, 120, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(2);
		assertThat(token).isEqualTo(new SemanticTokenData(121, 122, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(3);
		assertThat(token).isEqualTo(new SemanticTokenData(123, 124, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("*");

		token = tokens.get(4);
		assertThat(token).isEqualTo(new SemanticTokenData(125, 126, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("*");

		token = tokens.get(5);
		assertThat(token).isEqualTo(new SemanticTokenData(127, 130, "enum", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("THU");

		token = tokens.get(6);
		assertThat(token).isEqualTo(new SemanticTokenData(130, 131, "method", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("L");

	}

	@Test
	void thirdToLasttDayOfMonthAtMidnight() throws Exception {
		String source = """
				package my.package

				import org.springframework.scheduling.annotation.Scheduled;

				public class A {

					@Scheduled(cron="0 0 0 L-3 * *")
					void foo() {}

				}
				""";

		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri()
				.toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "A.java", jp);

		assertThat(cu).isNotNull();

		List<SemanticTokenData> tokens = computeTokens(cu);

		assertThat(tokens.size()).isEqualTo(8);

		SemanticTokenData token = tokens.get(0);
		assertThat(token).isEqualTo(new SemanticTokenData(117, 118, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(1);
		assertThat(token).isEqualTo(new SemanticTokenData(119, 120, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(2);
		assertThat(token).isEqualTo(new SemanticTokenData(121, 122, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(3);
		assertThat(token).isEqualTo(new SemanticTokenData(123, 124, "method", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("L");

		token = tokens.get(4);
		assertThat(token).isEqualTo(new SemanticTokenData(124, 125, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("-");

		token = tokens.get(5);
		assertThat(token).isEqualTo(new SemanticTokenData(125, 126, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("3");

		token = tokens.get(6);
		assertThat(token).isEqualTo(new SemanticTokenData(127, 128, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("*");

		token = tokens.get(7);
		assertThat(token).isEqualTo(new SemanticTokenData(129, 130, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("*");
	}

	@Test
	void errors_1() throws Exception {
		String source = """
				package my.package

				import org.springframework.scheduling.annotation.Scheduled;

				public class A {

					@Scheduled(cron="0 0 0 8LW * Foo#bar")
					void foo() {}

				}
				""";

		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri()
				.toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "A.java", jp);

		assertThat(cu).isNotNull();

		List<SemanticTokenData> tokens = computeTokens(cu);

		assertThat(tokens.size()).isEqualTo(9);

		SemanticTokenData token = tokens.get(0);
		assertThat(token).isEqualTo(new SemanticTokenData(117, 118, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(1);
		assertThat(token).isEqualTo(new SemanticTokenData(119, 120, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(2);
		assertThat(token).isEqualTo(new SemanticTokenData(121, 122, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(3);
		assertThat(token).isEqualTo(new SemanticTokenData(123, 124, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("8");

		token = tokens.get(4);
		assertThat(token).isEqualTo(new SemanticTokenData(124, 126, "method", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("LW");

		token = tokens.get(5);
		assertThat(token).isEqualTo(new SemanticTokenData(127, 128, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("*");

		token = tokens.get(6);
		assertThat(token).isEqualTo(new SemanticTokenData(129, 132, "enum", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("Foo");

		token = tokens.get(7);
		assertThat(token).isEqualTo(new SemanticTokenData(132, 133, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("#");

		token = tokens.get(8);
		assertThat(token).isEqualTo(new SemanticTokenData(133, 136, "enum", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("bar");
	}

	@Test
	void errors_2() throws Exception {
		String source = """
				package my.package

				import org.springframework.scheduling.annotation.Scheduled;

				public class A {

					@Scheduled(cron="qq#3 0 Blah 1-88LW * JUL-MARCH")
					void foo() {}

				}
				""";

		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri()
				.toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "A.java", jp);

		assertThat(cu).isNotNull();

		List<SemanticTokenData> tokens = computeTokens(cu);

		assertThat(tokens.size()).isEqualTo(13);

		SemanticTokenData token = tokens.get(0);
		assertThat(token).isEqualTo(new SemanticTokenData(117, 119, "enum", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("qq");

		token = tokens.get(1);
		assertThat(token).isEqualTo(new SemanticTokenData(119, 120, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("#");

		token = tokens.get(2);
		assertThat(token).isEqualTo(new SemanticTokenData(120, 121, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("3");

		token = tokens.get(3);
		assertThat(token).isEqualTo(new SemanticTokenData(122, 123, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("0");

		token = tokens.get(4);
		assertThat(token).isEqualTo(new SemanticTokenData(124, 128, "enum", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("Blah");

		token = tokens.get(5);
		assertThat(token).isEqualTo(new SemanticTokenData(129, 130, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("1");

		token = tokens.get(6);
		assertThat(token).isEqualTo(new SemanticTokenData(130, 131, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("-");

		token = tokens.get(7);
		assertThat(token).isEqualTo(new SemanticTokenData(131, 133, "number", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("88");

		token = tokens.get(8);
		assertThat(token).isEqualTo(new SemanticTokenData(133, 135, "method", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("LW");

		token = tokens.get(9);
		assertThat(token).isEqualTo(new SemanticTokenData(136, 137, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("*");

		token = tokens.get(10);
		assertThat(token).isEqualTo(new SemanticTokenData(138, 141, "enum", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("JUL");

		token = tokens.get(11);
		assertThat(token).isEqualTo(new SemanticTokenData(141, 142, "operator", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("-");

		token = tokens.get(12);
		assertThat(token).isEqualTo(new SemanticTokenData(142, 147, "enum", new String[0]));
		assertThat(source.substring(token.getStart(), token.getEnd())).isEqualTo("MARCH");
	}
	
	@Test
	void noTokens_SPEL() throws Exception {
		String source = """
				package my.package

				import org.springframework.scheduling.annotation.Scheduled;

				public class A {

					@Scheduled(cron="  #{demo.cron} ")
					void foo() {}

				}
				""";

		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri()
				.toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "A.java", jp);

		assertThat(cu).isNotNull();

		List<SemanticTokenData> tokens = computeTokens(cu);

		assertThat(tokens.size()).isEqualTo(0);
	}

	@Test
	void noTokens_PropertyHolder() throws Exception {
		String source = """
				package my.package

				import org.springframework.scheduling.annotation.Scheduled;

				public class A {

					@Scheduled(cron="  ${demo.cron} ")
					void foo() {}

				}
				""";

		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri()
				.toASCIIString();
		CompilationUnit cu = CompilationUnitCache.parse2(source.toCharArray(), uri, "A.java", jp);

		assertThat(cu).isNotNull();

		List<SemanticTokenData> tokens = computeTokens(cu);

		assertThat(tokens.size()).isEqualTo(0);
	}
}
