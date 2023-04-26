/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.AdHocPropertyHarnessTestConf;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import({ AdHocPropertyHarnessTestConf.class, ValueCompletionTest.TestConf.class })
public class PropertyValueAnnotationDefProviderTest {

	@Autowired
	private BootLanguageServerHarness harness;
	@Autowired
	private IJavaProject testProject;

	private Set<Path> createdFiles = new HashSet<>();

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);
	}

	@AfterEach
	public void tearDown() throws Exception {
		for (Path f : createdFiles) {
			Files.deleteIfExists(f);
		}
		createdFiles.clear();
	}

	private Path projectFile(String relativePath, String content) throws IOException {
		Path projectPath = Paths.get(testProject.getLocationUri());
		Path filePath = projectPath.resolve(relativePath);
		Files.createDirectories(filePath.getParent());
		Files.write(filePath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		createdFiles.add(filePath);
		return filePath;
	}

	@Test
	void propertiesCase() throws Exception {
		Path propertiesFilePath = projectFile("src/main/resources/application.properties", "some.prop=5");
		Editor editor = harness.newEditor(LanguageId.JAVA, """
				package org.test;

				import org.springframework.beans.factory.annotation.Value;

				public class TestValueCompletion {

					@Value("${some.prop}")
					private String value1;
				}""");

		LocationLink expectedLocation = new LocationLink(propertiesFilePath.toUri().toASCIIString(),
				new Range(new Position(0, 0), new Position(0, 11)), new Range(new Position(0, 10), new Position(0, 11)),
				new Range(new Position(6, 8), new Position(6, 22)));

		editor.assertLinkTargets("some.prop", List.of(expectedLocation));
	}

	@Test
	void yamlCase() throws Exception {
		Path yamlFilePath = projectFile("src/main/resources/application.yml", """
				some:
				  prop: 5
				""");
		Editor editor = harness.newEditor(LanguageId.JAVA, """
				package org.test;

				import org.springframework.beans.factory.annotation.Value;

				public class TestValueCompletion {

					@Value("${some.prop}")
					private String value1;
				}""");

		LocationLink expectedLocation = new LocationLink(yamlFilePath.toUri().toASCIIString(),
				new Range(new Position(1, 2), new Position(1, 9)), new Range(new Position(1, 8), new Position(1, 9)),
				new Range(new Position(6, 8), new Position(6, 22)));

		editor.assertLinkTargets("some.prop", List.of(expectedLocation));
	}
	
	@Test 
	void combinedCase() throws Exception {
		Path propertiesFilePath = projectFile("src/main/resources/application.properties", "some.prop=5");
		Path yamlFilePath = projectFile("src/main/resources/application.yml", """
				some:
				  prop: 5
				""");
		Editor editor = harness.newEditor(LanguageId.JAVA, """
				package org.test;

				import org.springframework.beans.factory.annotation.Value;

				public class TestValueCompletion {

					@Value("${some.prop}")
					private String value1;
				}""");

		LocationLink expectedPropsLocation = new LocationLink(propertiesFilePath.toUri().toASCIIString(),
				new Range(new Position(0, 0), new Position(0, 11)), new Range(new Position(0, 10), new Position(0, 11)),
				new Range(new Position(6, 8), new Position(6, 22)));
		LocationLink expectedYamlLocation = new LocationLink(yamlFilePath.toUri().toASCIIString(),
				new Range(new Position(1, 2), new Position(1, 9)), new Range(new Position(1, 8), new Position(1, 9)),
				new Range(new Position(6, 8), new Position(6, 22)));
		
		editor.assertLinkTargets("some.prop", List.of(expectedYamlLocation, expectedPropsLocation));

	}
}
