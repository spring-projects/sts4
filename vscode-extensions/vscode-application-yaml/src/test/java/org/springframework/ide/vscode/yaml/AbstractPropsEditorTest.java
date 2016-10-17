package org.springframework.ide.vscode.yaml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.springframework.ide.vscode.boot.properties.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.testharness.Editor;
import org.springframework.ide.vscode.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.testharness.TestProject;
import org.springframework.ide.vscode.yaml.PropertyIndexHarness.ItemConfigurer;

import io.typefox.lsapi.CompletionItem;

public class AbstractPropsEditorTest {
	
	private PropertyIndexHarness md;
	private LanguageServerHarness harness;
	
	public Editor newEditor(String contents) throws Exception {
		return harness.newEditor(contents);
	}
	
	@Before
	public void setup() throws Exception {
		md = new PropertyIndexHarness();
		harness = new LanguageServerHarness(ApplicationYamlLanguageServer::new);
		harness.intialize(null);
	}
	
	public ItemConfigurer data(String id, String type, Object deflt, String description, String... sources) {
		return md.data(id, type, deflt, description, sources);
	}
	
	public void defaultTestData() {
		md.defaultTestData();
	}
	
	public TestProject createPredefinedMavenProject(String string) {
		notImplemented();
		return null;
	}
	
	public void useProject(TestProject p) {
		notImplemented(); //only tests that don't require project context / classpath work for now
	}
	
	/**
	 * Simulates applying the first completion to a text buffer and checks the result.
	 */
	public void assertCompletion(String textBefore, String expectTextAfter) throws Exception {
		harness.assertCompletion(textBefore, expectTextAfter);
	}

	private void notImplemented() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Checks that completions contains a completion with a given display string and that that completion
	 * has a info hover that contains a given snippet of text.
	 */
	public void assertCompletionWithInfoHover(String editorText, String expectLabel, String expectInfoSnippet) throws Exception {
		notImplemented();
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
