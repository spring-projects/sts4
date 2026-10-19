/*******************************************************************************
 * Copyright (c) 2026 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.InlayHint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Feature flags for the editor features that are shared by several providers (semantic tokens, inlay hints),
 * checked through the language server.
 *
 * @author Martin Lippert
 */
@SuppressWarnings("deprecation")
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class FeatureFlagEditorFeaturesTest {

	@Autowired BootLanguageServerHarness harness;

	private final ProjectsHarness projects = ProjectsHarness.INSTANCE;
	private MavenJavaProject jp;

	@BeforeEach
	void setup() throws Exception {
		jp = projects.mavenProject("boot-mysql");
		harness.useProject(jp);
	}

	private void turnOff(String id) throws Exception {
		harness.changeConfiguration(Map.of("boot-java", Map.of(
				"jpql", true,
				"feature-flags", Map.of(id, false))));
	}

	private Editor javaEditor(String source) throws Exception {
		String uri = Paths.get(jp.getLocationUri()).resolve("src/main/resource/my/package/A.java").toUri().toASCIIString();
		return harness.newEditor(LanguageId.JAVA, source, uri);
	}

	private static final String QUERY_SOURCE = """
			package my.package

			import org.springframework.data.jpa.repository.Query;

			public interface OwnerRepository {

				@Query(value="SELECT o FROM Owner o WHERE o.lastName = ?1")
				void findByLastName(String lastName);
			}
			""";

	private static final String CRON_SOURCE = """
			package my.package

			import org.springframework.scheduling.annotation.Scheduled;

			public class A {

				@Scheduled(cron="0 0 0 ? * 5#2")
				void foo() {}

			}
			""";

	private static final String SPEL_SOURCE = """
			package my.package

			import org.springframework.beans.factory.annotation.Value;

			public class A {

				@Value(value="#{new String('hello world').toUpperCase()}")
				String s;

			}
			""";

	private static boolean noneOf(List<?> list) {
		return list == null || list.isEmpty();
	}

	@Test
	void dataQueryInlayHints() throws Exception {
		turnOff("x-nothing");
		assertEquals(1, javaEditor(QUERY_SOURCE).getInlayHints().size());
	}

	@Test
	void dataQueryInlayHintsTurnedOffByFeatureFlag() throws Exception {
		turnOff("data-query-inlay-hints");
		assertTrue(noneOf(javaEditor(QUERY_SOURCE).getInlayHints()));
	}

	@Test
	void dataQueryTokensStayOnWhenOnlyInlayHintsAreOff() throws Exception {
		turnOff("data-query-inlay-hints");
		assertFalse(noneOf(javaEditor(QUERY_SOURCE).getSemanticTokensFull()));
	}

	@Test
	void dataQueryInlayHintsStayOnWhenOnlyTokensAreOff() throws Exception {
		turnOff("data-query-semantic-tokens");
		assertEquals(1, javaEditor(QUERY_SOURCE).getInlayHints().size());
	}

	@Test
	void dataQueryTokens() throws Exception {
		turnOff("x-nothing");
		assertFalse(noneOf(javaEditor(QUERY_SOURCE).getSemanticTokensFull()));
	}

	@Test
	void dataQueryTokensTurnedOffByFeatureFlag() throws Exception {
		turnOff("data-query-semantic-tokens");
		assertTrue(noneOf(javaEditor(QUERY_SOURCE).getSemanticTokensFull()));
	}

	@Test
	void cronTokens() throws Exception {
		turnOff("x-nothing");
		assertFalse(noneOf(javaEditor(CRON_SOURCE).getSemanticTokensFull()));
	}

	@Test
	void cronTokensTurnedOffByFeatureFlag() throws Exception {
		turnOff("cron-semantic-tokens");
		assertTrue(noneOf(javaEditor(CRON_SOURCE).getSemanticTokensFull()));
	}

	@Test
	void spelTokens() throws Exception {
		turnOff("x-nothing");
		assertFalse(noneOf(javaEditor(SPEL_SOURCE).getSemanticTokensFull()));
	}

	@Test
	void spelTokensTurnedOffByFeatureFlag() throws Exception {
		turnOff("spel-semantic-tokens");
		assertTrue(noneOf(javaEditor(SPEL_SOURCE).getSemanticTokensFull()));
	}

}
