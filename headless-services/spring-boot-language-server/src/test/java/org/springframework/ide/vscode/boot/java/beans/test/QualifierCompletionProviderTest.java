/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.AnnotationMetadata;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Martin Lippert
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class QualifierCompletionProviderTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringMetamodelIndex springIndex;
	@Autowired private SpringSymbolIndex indexer;

	private File directory;
	private IJavaProject project;
	private Bean[] indexedBeans;
	private String tempJavaDocUri;
	private Bean bean1;
	private Bean bean2;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-spring-indexing/").toURI());

		String projectDir = directory.toURI().toString();
		project = projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
		
		indexedBeans = springIndex.getBeansOfProject(project.getElementName());
		
        tempJavaDocUri = directory.toPath().resolve("src/main/java/org/test/TempClass.java").toUri().toString();
        AnnotationMetadata annotationBean1 = new AnnotationMetadata("org.springframework.beans.factory.annotation.Qualifier", false, Map.of("value", new String[] {"quali1"}));
        AnnotationMetadata annotationBean2 = new AnnotationMetadata("org.springframework.beans.factory.annotation.Qualifier", false, Map.of("value", new String[] {"quali2"}));
        
		bean1 = new Bean("bean1", "type1", new Location(tempJavaDocUri, new Range(new Position(1,1), new Position(1, 20))), null, null, new AnnotationMetadata[] {annotationBean1});
		bean2 = new Bean("bean2", "type2", new Location(tempJavaDocUri, new Range(new Position(1,1), new Position(1, 20))), null, null, new AnnotationMetadata[] {annotationBean2});
		
		springIndex.updateBeans(project.getElementName(), new Bean[] {bean1, bean2});
	}
	
	@AfterEach
	public void restoreIndexState() {
		this.springIndex.updateBeans(project.getElementName(), indexedBeans);
	}
	
	@Test
	public void testQualifierCompletionWithoutQuotesWithoutPrefixAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(<*>)", new String[] {"quali1", "quali2", "bean1", "bean2"}, 0, "@Qualifier(\"quali1\"<*>)", false);
	}

	@Test
	public void testQualifierCompletionWithoutQuotesWithoutPrefixAtTypeDeclaration() throws Exception {
		assertCompletions("@Qualifier(<*>)", new String[] {"quali1", "quali2"}, 0, "@Qualifier(\"quali1\"<*>)", true);
	}

	@Test
	public void testQualifierCompletionWithoutQuotesWithPrefixAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(be<*>)", 2, "@Qualifier(\"bean1\"<*>)", false);
	}

	@Test
	public void testQualifierCompletionWithoutQuotesWithPrefixFromExistingQualifierAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(qu<*>)", new String[] {"quali1", "quali2"}, 0, "@Qualifier(\"quali1\"<*>)", false);
	}

	@Test
	@Disabled // TODO: implement this case
	public void testQualifierCompletionWithoutQuotesWithAttributeNameAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(value=<*>)", 4, "@Qualifier(value=\"quali1\"<*>)", false);
	}

	@Test
	public void testQualifierCompletionWithoutQuotesWithAttributeNameAtTypeDeclaration() throws Exception {
		assertCompletions("@Qualifier(value=<*>)", 2, "@Qualifier(value=\"quali1\"<*>)", true);
	}

	@Test
	@Disabled // TODO: implement this case
	public void testQualifierCompletionWithoutQuotesWithAttributeNameAndSpacesAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(value = <*>)", 4, "@Qualifier(value = \"quali1\"<*>)", false);
	}

	@Test
	public void testQualifierCompletionWithoutQuotesWithAttributeNameAndSpacesAtTypeDeclaration() throws Exception {
		assertCompletions("@Qualifier(value = <*>)", 2, "@Qualifier(value = \"quali1\"<*>)", true);
	}

	@Test
	public void testQualifierCompletionInsideOfQuotesWithoutPrefixAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(\"<*>\")", 4, "@Qualifier(\"quali1<*>\")", false);
	}

	@Test
	public void testQualifierCompletionInsideOfQuotesWithPrefixAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(\"be<*>\")", 2, "@Qualifier(\"bean1<*>\")", false);
	}

	@Test
	public void testQualifierCompletionInsideOfQuotesWithPrefixButWithoutMatchesAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(\"XXX<*>\")", 0, null, false);
	}

	@Test
	public void testQualifierCompletionOutsideOfAnnotation1AtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(\"XXX\")<*>", 0, null, false);
	}

	@Test
	public void testQualifierCompletionOutsideOfAnnotation2AtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier<*>(\"XXX\")", 0, null, false);
	}

	@Test
	public void testQualifierCompletionInsideOfQuotesWithPrefixAndReplacedPostfixAtInjecitonPoint() throws Exception {
		assertCompletions("@Qualifier(\"be<*>xxx\")", 2, "@Qualifier(\"bean1<*>\")", false);
	}
	
	private void assertCompletions(String completionLine, int noOfExpectedCompletions, String expectedCompletedLine, boolean onType) throws Exception {
		assertCompletions(completionLine, noOfExpectedCompletions, null, 0, expectedCompletedLine, onType);
	}

	private void assertCompletions(String completionLine, String[] expectedCompletions, int chosenCompletion, String expectedCompletedLine, boolean onType) throws Exception {
		assertCompletions(completionLine, expectedCompletions.length, expectedCompletions, chosenCompletion, expectedCompletedLine, onType);
	}

	private void assertCompletions(String completionLine, int noOfExcpectedCompletions, String[] expectedCompletions, int chosenCompletion, String expectedCompletedLine, boolean onType) throws Exception {
		String editorContent = createEditorContent(completionLine, onType);
		Editor editor = harness.newEditor(LanguageId.JAVA, editorContent, tempJavaDocUri);

        List<CompletionItem> completions = editor.getCompletions();
        assertEquals(noOfExcpectedCompletions, completions.size());

        if (expectedCompletions != null) {
	        String[] completionItems = completions.stream()
	        	.map(item -> item.getLabel())
	        	.toArray(size -> new String[size]);
	        
	        assertArrayEquals(expectedCompletions, completionItems);
        }
        
        if (noOfExcpectedCompletions > 0) {
	        editor.apply(completions.get(chosenCompletion));
	        assertEquals(createEditorContent(expectedCompletedLine, onType), editor.getText());
        }
	}
	
	private String createEditorContent(String placeholderValue, boolean onType) {
		if (onType) {
			return """
					package org.test;

	        		import org.springframework.stereotype.Component;
					import org.springframework.beans.factory.annotation.Qualifier;

					@Component
					""" +
					placeholderValue + "\n" +
					"""
					public class TestDependsOnClass {
					}
					""";
		}
		else {
			return """
					package org.test;

	        		import org.springframework.stereotype.Component;
					import org.springframework.beans.factory.annotation.Qualifier;

					@Component
					public class TestDependsOnClass {
					
						public TestDependsOnClass(""" + placeholderValue + " Object someBean) {" +
					"""
						}
					
					}
					""";
		}
	}


}
