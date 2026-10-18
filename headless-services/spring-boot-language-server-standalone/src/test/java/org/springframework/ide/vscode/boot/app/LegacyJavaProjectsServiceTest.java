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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.DiagnosticService;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;

/**
 * Tests for the Maven project discovery of {@link LegacyJavaProjectsService}.
 */
class LegacyJavaProjectsServiceTest {

	@TempDir Path workspace;

	private LegacyJavaProjectsService service;

	@BeforeEach
	void setup() {
		SimpleLanguageServer server = mock(SimpleLanguageServer.class);
		SimpleWorkspaceService workspaceService = mock(SimpleWorkspaceService.class);
		when(workspaceService.getFileObserver()).thenReturn(new BasicFileObserver());
		when(server.getWorkspaceService()).thenReturn(workspaceService);
		when(server.getDiagnosticService()).thenReturn(mock(DiagnosticService.class));
		service = new LegacyJavaProjectsService(server);
	}

	@Test
	void singleModuleProject_registersOnlyRoot() throws Exception {
		writePom(workspace, "root", "jar", "");

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(pom(workspace));
	}

	@Test
	void multiModuleProject_registersRootAndModules() throws Exception {
		writePom(workspace, "root", "pom", modules("module-a", "module-b"));
		writePom(workspace.resolve("module-a"), "module-a", "jar", "");
		writePom(workspace.resolve("module-b"), "module-b", "jar", "");

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(workspace),
				pom(workspace.resolve("module-a")),
				pom(workspace.resolve("module-b")));
	}

	@Test
	void nestedReactor_registersModulesOfAllLevels() throws Exception {
		writePom(workspace, "root", "pom", modules("services"));
		writePom(workspace.resolve("services"), "services", "pom", modules("service-a"));
		writePom(workspace.resolve("services/service-a"), "service-a", "jar", "");

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(workspace),
				pom(workspace.resolve("services")),
				pom(workspace.resolve("services/service-a")));
	}

	@Test
	void moduleInActiveProfile_isRegistered() throws Exception {
		writePom(workspace, "root", "pom", """
				<profiles>
					<profile>
						<id>all</id>
						<activation><activeByDefault>true</activeByDefault></activation>
						<modules><module>profile-module</module></modules>
					</profile>
				</profiles>
				""");
		writePom(workspace.resolve("profile-module"), "profile-module", "jar", "");

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(workspace),
				pom(workspace.resolve("profile-module")));
	}

	@Test
	void moduleInInactiveProfile_isNotRegistered() throws Exception {
		writePom(workspace, "root", "pom", """
				<profiles>
					<profile>
						<id>extra</id>
						<modules><module>extra-module</module></modules>
					</profile>
				</profiles>
				""");
		writePom(workspace.resolve("extra-module"), "extra-module", "jar", "");

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(pom(workspace));
	}

	@Test
	void interpolatedModulePath_isResolved() throws Exception {
		writePom(workspace, "root", "pom", """
				<properties><app.module>app</app.module></properties>
				<modules><module>${app.module}</module></modules>
				""");
		writePom(workspace.resolve("app"), "app", "jar", "");

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(workspace),
				pom(workspace.resolve("app")));
	}

	@Test
	void missingModuleDirectory_isSkipped() throws Exception {
		writePom(workspace, "root", "pom", modules("present", "absent"));
		writePom(workspace.resolve("present"), "present", "jar", "");

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(workspace),
				pom(workspace.resolve("present")));
	}

	@Test
	void malformedModulePom_doesNotBlockSiblings() throws Exception {
		writePom(workspace, "root", "pom", modules("broken", "healthy"));
		Files.createDirectories(workspace.resolve("broken"));
		Files.writeString(workspace.resolve("broken/pom.xml"), "<project>not a valid pom");
		writePom(workspace.resolve("healthy"), "healthy", "jar", "");

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(workspace),
				pom(workspace.resolve("broken")),
				pom(workspace.resolve("healthy")));
	}

	@Test
	void cyclicModuleDeclaration_terminatesAndRegistersEachPomOnce() throws Exception {
		writePom(workspace, "root", "pom", modules("child"));
		writePom(workspace.resolve("child"), "child", "pom", modules(".."));

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(workspace),
				pom(workspace.resolve("child")));
	}

	@Test
	void sameModuleUnderDifferentSpellings_isRegisteredOnce() throws Exception {
		writePom(workspace, "root", "pom", modules("module-a", "./module-a", "module-b/../module-a"));
		writePom(workspace.resolve("module-a"), "module-a", "jar", "");
		Files.createDirectories(workspace.resolve("module-b"));

		service.initializeProject(workspace.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(workspace),
				pom(workspace.resolve("module-a")));
	}

	@Test
	void moduleOutsideWorkspace_isRegistered() throws Exception {
		Path root = workspace.resolve("root");
		writePom(root, "root", "pom", modules("../shared"));
		writePom(workspace.resolve("shared"), "shared", "jar", "");

		service.initializeProject(root.toFile());

		assertThat(registeredPoms()).containsExactlyInAnyOrder(
				pom(root),
				pom(workspace.resolve("shared")));
	}

	@Test
	void documentInModule_resolvesToRegisteredProject() throws Exception {
		writePom(workspace, "root", "pom", modules("./module-a"));
		writePom(workspace.resolve("module-a"), "module-a", "jar", "");
		Path javaFile = workspace.resolve("module-a/src/main/java/Foo.java");
		Files.createDirectories(javaFile.getParent());
		Files.writeString(javaFile, "class Foo {}");

		service.initializeProject(workspace.toFile());
		int registered = service.all().size();

		Optional<IJavaProject> project = service.find(new TextDocumentIdentifier(javaFile.toUri().toString()));

		assertThat(project).isPresent();
		assertThat(((MavenJavaProject) project.get()).pom().toPath()).isEqualTo(pom(workspace.resolve("module-a")));
		assertThat(service.all()).hasSize(registered);
	}

	private List<Path> registeredPoms() {
		return service.all().stream()
				.map(p -> ((MavenJavaProject) p).pom().toPath())
				.toList();
	}

	private static Path pom(Path dir) {
		return dir.resolve("pom.xml").toAbsolutePath().normalize();
	}

	private static String modules(String... modules) {
		StringBuilder sb = new StringBuilder("<modules>");
		for (String module : modules) {
			sb.append("<module>").append(module).append("</module>");
		}
		return sb.append("</modules>").toString();
	}

	private static void writePom(Path dir, String artifactId, String packaging, String extra) throws IOException {
		Files.createDirectories(dir);
		Files.writeString(dir.resolve("pom.xml"), """
				<?xml version="1.0" encoding="UTF-8"?>
				<project xmlns="http://maven.apache.org/POM/4.0.0">
					<modelVersion>4.0.0</modelVersion>
					<groupId>test</groupId>
					<artifactId>%s</artifactId>
					<version>1.0.0</version>
					<packaging>%s</packaging>
					%s
				</project>
				""".formatted(artifactId, packaging, extra));
	}

}
