package org.springframework.ide.vscode.languageserver.testharness;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;
import org.springframework.ide.vscode.commons.util.HtmlSnippet;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Class meant to be used in writing tests that require test projects to be populated
 * from some data on the classpath.
 */
public class TestProjectHarness {

	private static final class TestProject implements IJavaProject {
		private Path location;

		public TestProject(Path location) {
			this.location = location;
		}

		@Override
		public HtmlSnippet getJavaDoc() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getElementName() {
			return location.toFile().getName();
		}

		@Override
		public boolean exists() {
			return true;
		}

		@Override
		public IClasspath getClasspath() {
			return () -> location;
		}

		@Override
		public IType findType(String fqName) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public Cache<String, IJavaProject> cache = CacheBuilder.newBuilder().build();

	public IJavaProject mavenProject(String name) throws Exception {
		Path testProjectPath = Paths.get(TestProjectHarness.class.getResource("/"+name).toURI());
		assertTrue(Files.exists(testProjectPath));
		if (!Files.exists(testProjectPath.resolve("classpath.txt"))) {
			testProjectPath.resolve("mvnw").toFile().setExecutable(true);
			ExternalProcess process = new ExternalProcess(testProjectPath.toFile(), new ExternalCommand("./mvnw", "clean", "package"), true);
			if (process.getExitValue() != 0) {
				throw new RuntimeException("Failed to build test project");
			}
		}
		return new TestProject(testProjectPath);
	}

}
