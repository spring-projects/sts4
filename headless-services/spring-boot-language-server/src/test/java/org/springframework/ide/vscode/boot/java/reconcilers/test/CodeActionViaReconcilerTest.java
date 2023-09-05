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
package org.springframework.ide.vscode.boot.java.reconcilers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.CodeAction;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class CodeActionViaReconcilerTest {
	
	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;

	private File directory;
	@Autowired private SpringSymbolIndex indexer;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);
		
		String changedSettings = "{\"boot-java\": {\"validation\": {\"java\": { \"reconcilers\": true}}}}";
		JsonElement settingsAsJson = new Gson().fromJson(changedSettings, JsonElement.class);
		harness.changeConfiguration(new Settings(settingsAsJson));

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-spring-validations/").toURI());

		String projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

    @Test
    void codeActionsFromReconcilingProblems() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/BeanMethodNotPublic1.java").toUri().toString();
        Editor editor = harness.newEditor(LanguageId.JAVA, """
		package org.test;
		
		import org.springframework.context.annotation.Bean;
		import org.springframework.context.annotation.Configuration;
		
		@Configuration
		class BeanMethodNotPublic1 {
		
			@Bean
			public BeanClass1 publicBeanMethod() {
				return new BeanClass1();
			}
			
			@Bean
			BeanClass2 nonPublicBeanMethod() {
				return new BeanClass2();
			}
		
		}
		""", docUri);
        
        List<CodeAction> codeActions = editor.getCodeActions("public", 1);
        assertEquals(0, codeActions.size());
        
        Diagnostic problem = editor.assertProblem("public");
        assertNotNull(problem);
        assertEquals(Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD.getCode(), problem.getCode().getLeft());
        assertEquals(Boot2JavaProblemType.JAVA_PUBLIC_BEAN_METHOD.getLabel(), problem.getMessage());
        
        codeActions = editor.getCodeActions(problem);
        assertEquals(3, codeActions.size());
		String changedSettings = "{\"spring-boot\": {\"ls\": {\"problem\": { \"boot2\": { \"JAVA_PUBLIC_BEAN_METHOD\": \"IGNORE\"}}}}}";
		JsonElement settingsAsJson = new Gson().fromJson(changedSettings, JsonElement.class);
		harness.changeConfiguration(new Settings(settingsAsJson));

        editor.assertProblems();
        
        codeActions = editor.getCodeActions("public", 1);
        assertEquals(3, codeActions.size());
    }


}
