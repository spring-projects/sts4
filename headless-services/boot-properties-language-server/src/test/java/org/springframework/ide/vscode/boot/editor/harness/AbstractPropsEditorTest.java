/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.editor.harness;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.Before;
import org.springframework.ide.vscode.boot.editor.harness.PropertyIndexHarness.ItemConfigurer;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.AbstractJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

public abstract class AbstractPropsEditorTest {
	
	public static final String INTEGER = Integer.class.getName();
	public static final String BOOLEAN = Boolean.class.getName();
	public static final String STRING = String.class.getName();
	
	private ProjectsHarness projects = ProjectsHarness.INSTANCE;

	protected PropertyIndexHarness md;
	protected final CompositeJavaProjectFinder javaProjectFinder = new CompositeJavaProjectFinder(Arrays.asList(new AbstractJavaProjectFinder() {
		@Override
		public IJavaProject find(File doc) {
			return getTestProject();
		}
	}));
			
	private LanguageServerHarness harness;
	private IJavaProject testProject;
	private TypeUtil typeUtil;	

	protected TypeUtilProvider typeUtilProvider = (IDocument doc) -> {
		if (typeUtil==null) {
			typeUtil = new TypeUtil(testProject);
		}
		return typeUtil;
	};

	public Editor newEditor(String contents) throws Exception {
		return harness.newEditor(contents);
	}
	
	private IJavaProject getTestProject() {
		return testProject;
	}

	@Before
	public void setup() throws Exception {
		md = new PropertyIndexHarness();
		harness = new LanguageServerHarness(this::newLanguageServer) {
			protected String getFileExtension() {
				return AbstractPropsEditorTest.this.getFileExtension();
			}
		};
		harness.intialize(null);
	}
	
	/**
	 * Determines the extension used to create temporary uris for editor contents documents.
	 * Tests need to control this if the language server behavior they are testing varies depending on the
	 * extension (e.g. different validation, completions etc. for .yml versus .properties
	 */
	protected abstract String getFileExtension();
	
	protected abstract SimpleLanguageServer newLanguageServer();
	
	public ItemConfigurer data(String id, String type, Object deflt, String description, String... sources) {
		return md.data(id, type, deflt, description, sources);
	}
	
	public void defaultTestData() {
		md.defaultTestData();
	}
	
	public MavenJavaProject createPredefinedMavenProject(String name) throws Exception {
		return projects.mavenProject(name);
	}
	
	public void useProject(IJavaProject p) throws Exception {
		md.useProject(p);
		this.testProject = p;
		this.typeUtil = null;
	}
	
	/**
	 * Simulates applying the first completion to a text buffer and checks the result.
	 */
	public void assertCompletion(String textBefore, String expectTextAfter) throws Exception {
		harness.assertCompletion(textBefore, expectTextAfter);
	}

	public void assertCompletionDisplayString(String editorContents, String expected) throws Exception {
		harness.assertCompletionDisplayString(editorContents, expected);
	}

	@Deprecated
	public void assertCompletionWithInfoHover(String editorText, String expectLabel, String expectInfoSnippet) throws Exception {
		Editor editor = newEditor(editorText);
		editor.assertCompletionDetails(expectLabel, null, expectInfoSnippet);
	}
	
	/**
	 * Checks that completions contains a completion with a given display string, detail and documentation text
	 */
	public void assertCompletionDetails(String editorText, String expectLabel, String expectDetail, String expectDocumenation) throws Exception {
		Editor editor = newEditor(editorText);
		editor.assertCompletionDetails(expectLabel, expectDetail, expectDocumenation);
	}
	
	public boolean isEmptyMetadata() {
		return md.isEmpty();
	}

	/**
	 * Checks that applying completions to a given 'textBefore' editor content produces the
	 * expected results.
	 */
	public void assertCompletions(String textBefore, String... expectTextAfter) throws Exception {
		harness.assertCompletions(textBefore, expectTextAfter);
	}

	public void assertNoCompletions(String text) throws Exception {
		assertCompletions(text /*NONE*/);
	}

	/**
	 * Checks that completions contains a completion with a given display string (and check that
	 * it applies as expected).
	 */
	public void assertCompletionWithLabel(String textBefore, String expectLabel, String expectTextAfter) throws Exception {
		Editor editor = newEditor(textBefore);
		List<CompletionItem> completions = editor.getCompletions();
		CompletionItem completion = assertCompletionWithLabel(expectLabel, completions);
		editor.apply(completion);
		assertEquals(expectTextAfter, editor.getText());
	}

	private CompletionItem assertCompletionWithLabel(String expectLabel, List<CompletionItem> completions) {
		StringBuilder found = new StringBuilder();
		for (CompletionItem c : completions) {
			String actualLabel = c.getLabel();
			found.append(actualLabel+"\n");
			if (actualLabel.equals(expectLabel)) {
				return c;
			}
		}
		fail("No completion found with label '"+expectLabel+"' in:\n"+found);
		return null; //unreachable, but compiler doesn't know that.
	}

	public void assertCompletionCount(int expected, String editorText) throws Exception {
		Editor editor = newEditor(editorText);
		assertEquals(expected, editor.getCompletions().size());
	}

	public void assertCompletionsDisplayString(String editorText, String... completionsLabels) throws Exception {
		Editor editor = newEditor(editorText);
		List<CompletionItem> completions = editor.getCompletions();
		String[] actualLabels = new String[completions.size()];
		for (int i = 0; i < actualLabels.length; i++) {
			actualLabels[i] = completions.get(i).getLabel();
		}
		assertElements(actualLabels, completionsLabels);
	}
	
	public void assertCompletionsDisplayStringAndDetail(String editorText, String[]...expectCompletions) throws Exception {
		String[] completionsLabels = new String[expectCompletions.length];
		String[] completionDetails = new String[expectCompletions.length];
		for (int i = 0; i < expectCompletions.length; i++) {
			completionsLabels[i] = expectCompletions[i][0];
			completionDetails[i] = expectCompletions[i][1];
		}
		Editor editor = newEditor(editorText);
		List<CompletionItem> completions = editor.getCompletions();
		String[] actualLabels = new String[completions.size()];
		String[] actualDetails = new String[completions.size()];
		for (int i = 0; i < completions.size(); i++) {
			actualLabels[i] = completions.get(i).getLabel();
			actualDetails[i] = completions.get(i).getDetail();
		}
		assertElements(actualLabels, completionsLabels);
		assertArrayEquals(actualDetails, completionDetails);
	}

	public SpringPropertyIndexProvider getIndexProvider() {
		return md.getIndexProvider();
	}
	
	@SafeVarargs
	public static <T> void assertElements(T[] actual, T... expect) {
		assertElements(Arrays.asList(actual), expect);
	}

	@SafeVarargs
	public static <T> void assertElements(Collection<T> actual, T... expect) {
		Set<T> expectedSet = new HashSet<T>(Arrays.asList(expect));

		for (T propVal : actual) {
			if (!expectedSet.remove(propVal)) {
				fail("Unexpected element: "+propVal);
			}
		}

		if (!expectedSet.isEmpty()) {
			StringBuilder missing = new StringBuilder();
			for (T propVal : expectedSet) {
				missing.append(propVal+"\n");
			}
			fail("Missing elements: \n"+missing);
		}
	}
	
	public void assertStyledCompletions(String editorText, StyledStringMatcher... expectStyles) throws Exception {
		Editor editor = newEditor(editorText);
		List<CompletionItem> completions = editor.getCompletions();
		assertEquals("Wrong number of elements", expectStyles.length, completions.size());
		for (int i = 0; i < expectStyles.length; i++) {
			CompletionItem completion = completions.get(i);
			throw new UnsupportedOperationException("Suport for styled labels not implemented");
//			StyledString actualLabel = getStyledDisplayString(completion);
//			expectStyles[i].match(actualLabel);
		}
	}


	public void deprecate(String key, String replacedBy, String reason) {
		md.deprecate(key, replacedBy, reason);
	}
	
	public void keyHints(String id, String... hintValues) {
		md.keyHints(id, hintValues);
	}

	public void valueHints(String id, String... hintValues) {
		md.valueHints(id, hintValues);
	}

}
