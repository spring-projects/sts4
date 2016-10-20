package org.springframework.ide.vscode.testharness;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.util.ExternalCommand;
import org.springframework.ide.vscode.util.ExternalProcess;
import org.springframework.ide.vscode.util.HtmlSnippet;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Class meant to be used in writing tests that require test projects to be populated
 * from some data on the classpath.
 */
public class TestProjectHarness {

	public Cache<String, IJavaProject> cache = CacheBuilder.newBuilder().build();

	public IJavaProject get(String name) throws Exception {
		Path testProjectPath = Paths.get(TestProjectHarness.class.getResource("/demo-1").toURI());
		assertTrue(Files.exists(testProjectPath));
		if (!Files.exists(testProjectPath.resolve("classpath.txt"))) {
			testProjectPath.resolve("mvnw").toFile().setExecutable(true);
			ExternalProcess process = new ExternalProcess(testProjectPath.toFile(), new ExternalCommand("./mvnw", "clean", "package"), true);
			if (process.getExitValue() != 0) {
				throw new RuntimeException("Failed to build test project");
			}
		}
		return new IJavaProject() {

			@Override
			public HtmlSnippet getJavaDoc() {
				return null;
			}

			@Override
			public String getElementName() {
				return name;
			}

			@Override
			public boolean exists() {
				return true;
			}

			@Override
			public Path getPath() {
				return testProjectPath;
			}

			@Override
			public IType findType(String string) {
				//throw new UnsupportedOperationException("Not yet implemented");
				return null;
			}
		};
	}

}
