/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.index.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.SpringIndexElement;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Martin Lippert
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class SpringIndexerBeanRegistrarTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private SpringMetamodelIndex springIndex;

	private File directory;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-framework-7-indexing/").toURI());

		String projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

    @Test
    void testSimpleBeanRegistration() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/com/example/MyBeanRegistrar.java").toUri().toString();

        Bean[] beans = springIndex.getBeansOfDocument(docUri);
        assertEquals(5, beans.length);
        
        Bean beanRegistrarBean = Arrays.stream(beans).filter(bean -> bean.getName().equals("myBeanRegistrar")).findFirst().get();
        assertEquals("com.example.MyBeanRegistrar", beanRegistrarBean.getType());
        
        List<SpringIndexElement> children = beanRegistrarBean.getChildren();
        assertEquals(4, children.size());
        
        Bean fooFoo = (Bean) children.get(0);
        assertEquals("fooFoo", fooFoo.getName());
        assertEquals("com.example.FooFoo", fooFoo.getType());

        Bean foo = (Bean) children.get(1);
        assertEquals("foo", foo.getName());
        assertEquals("com.example.Foo", foo.getType());

        Bean bar = (Bean) children.get(2);
        assertEquals("bar", bar.getName());
        assertEquals("com.example.Bar", bar.getType());

        Bean baz = (Bean) children.get(3);
        assertEquals("baz", baz.getName());
        assertEquals("com.example.Baz", baz.getType());
    }

}
