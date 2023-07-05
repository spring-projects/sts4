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
package org.springframework.ide.vscode.boot.modulith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class ModulithServiceTest {
	
	@Autowired private BootLanguageServerHarness harness;
	
	@Autowired private ModulithService modulithService;
	
	@Autowired private SpringSymbolIndex indexer;
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	
	private MavenJavaProject jp;
	
	@BeforeEach
	public void setup() throws Exception {
		// Use project harness with a customizer to land the project in the 'temp' folder rather than 'target/test-classes' because Modulith will filter everything with 'target/test-classes' out :-\ 
		jp =  projects.mavenProject("spring-modulith-example-full", p -> {});
		harness.useProject(jp);
		
		String projectDir = jp.getLocationUri().toASCIIString();

		// trigger project creation
		harness.getProjectFinder().find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}
	
	@AfterEach
	public void tearDown() {
		jp = null;
	}
	
    @Test
    void sanityTest() throws Exception {
    	List<AppModule> modules = modulithService.getModulesData(jp).get().modules;
    	assertEquals(2, modules.size());
    	AppModule orderModule = modules.get(0);
    	assertEquals("order", orderModule.name());
    	assertEquals("org.example.order", orderModule.basePackage());
    	assertEquals(6, orderModule.namedInterfaces().size());
    	AppModule inventoryModule = modules.get(1);
    	assertEquals("inventory", inventoryModule.name());
    	assertEquals("org.example.inventory", inventoryModule.basePackage());
    	assertEquals(0, inventoryModule.namedInterfaces().size());
    }


}
