/*******************************************************************************
 * Copyright (c) 2023, 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.conditionalonresource.test;

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
import org.springframework.ide.vscode.boot.java.conditionalonresource.ConditionalOnResourceDefinitionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import({ AdHocPropertyHarnessTestConf.class, ConditionalOnResourceCompletionTest.TestConf.class })
public class ConditionalOnResourceDefinitionProviderTest {

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
    void testFindClasspathResource() throws Exception {
        Path randomResourceFilePath = projectFile("src/main/resources/a-random-resource-root.md", "");
        Editor editor = harness.newEditor(LanguageId.JAVA, """
				package org.test;

				import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
				import org.springframework.context.annotation.Configuration;
				
				@Configuration
				@ConditionalOnResource("classpath:a-random-resource-root.md")
				public class TestConditionalOnResourceCompletion {

					private String value1;
				}""");

        LocationLink expectedLocation = new LocationLink(randomResourceFilePath.toUri().toASCIIString(),
                new Range(new Position(0, 0), new Position(0, 0)),
                new Range(new Position(0, 0), new Position(0, 0)),
                new Range(new Position(6, 23), new Position(6, 60)));

        editor.assertLinkTargets("classpath:a-random-resource-root.md", List.of(expectedLocation));
    }

}