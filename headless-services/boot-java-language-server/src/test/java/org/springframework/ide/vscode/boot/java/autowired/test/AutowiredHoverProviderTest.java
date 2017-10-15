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
package org.springframework.ide.vscode.boot.java.autowired.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.autowired.AutowiredHoverProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectManager;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectManager;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.PropertyIndexHarness;

/**
 * @author Martin Lippert
 */
public class AutowiredHoverProviderTest {

	private JavaProjectManager projectManager;
	private LanguageServerHarness<BootJavaLanguageServer> harness;
	private PropertyIndexHarness indexHarness;
	private BasicFileObserver fileObserver;

	@Before
	public void setup() throws Exception {
		projectManager = new CompositeJavaProjectManager(new JavaProjectManager[] {new MavenProjectManager(MavenCore.getDefault())});
		fileObserver = new BasicFileObserver();
		projectManager.setFileObserver(fileObserver);

		indexHarness = new PropertyIndexHarness();
		harness = new LanguageServerHarness<BootJavaLanguageServer>(new Callable<BootJavaLanguageServer>() {
			@Override
			public BootJavaLanguageServer call() throws Exception {
				BootJavaLanguageServer server = new BootJavaLanguageServer(projectManager, indexHarness.getIndexProvider());
				return server;
			}
		}) {
			@Override
			protected String getFileExtension() {
				return ".java";
			}
		};
	}

	@Test
	public void testLiveHoverHintForAutowiredOnConstructor() throws Exception {
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-autowired/").toURI());
		harness.intialize(directory);

		String docURI = "file://" + directory.getAbsolutePath() + "/src/main/java/org/test/MyAutowiredComponent.java";
		IJavaProject project = projectManager.find(directory);
		TextDocument document = createTempTextDocument(docURI);

		CompilationUnit cu = parse(document, project);

		int offset = document.toOffset(new Position(11, 4));
		ASTNode node = NodeFinder.perform(cu, offset, 0).getParent();

		AutowiredHoverProvider provider = new AutowiredHoverProvider();
		String beansJSON = new String(Files.readAllBytes(new File(directory, "runtime-bean-information.json").toPath()));

		Range hint = provider.getLiveHoverHint((Annotation)node, document, beansJSON);
		assertNotNull(hint);

		assertEquals(11, hint.getStart().getLine());
		assertEquals(1, hint.getStart().getCharacter());
		assertEquals(11, hint.getEnd().getLine());
		assertEquals(11, hint.getEnd().getCharacter());
	}

	@Test
	public void testNoLiveHoverHintForAutowiredOnConstructorWithNoLiveAppData() throws Exception {
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-autowired/").toURI());
		harness.intialize(directory);

		String docURI = "file://" + directory.getAbsolutePath() + "/src/main/java/org/test/MyAutowiredComponent.java";
		IJavaProject project = projectManager.find(directory);
		TextDocument document = createTempTextDocument(docURI);

		CompilationUnit cu = parse(document, project);

		int offset = document.toOffset(new Position(11, 4));
		ASTNode node = NodeFinder.perform(cu, offset, 0).getParent();

		AutowiredHoverProvider provider = new AutowiredHoverProvider();
		Range hint = provider.getLiveHoverHint((Annotation)node, document, (String)null);
		assertNull(hint);
	}

	@Test
	public void testNoLiveHoverHintForAutowiredOnConstructorWithWrongLiveAppData() throws Exception {
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotation-indexing-autowired/").toURI());
		harness.intialize(directory);

		String docURI = "file://" + directory.getAbsolutePath() + "/src/main/java/org/test/MyAutowiredComponent.java";
		IJavaProject project = projectManager.find(directory);
		TextDocument document = createTempTextDocument(docURI);

		CompilationUnit cu = parse(document, project);

		int offset = document.toOffset(new Position(11, 4));
		ASTNode node = NodeFinder.perform(cu, offset, 0).getParent();

		AutowiredHoverProvider provider = new AutowiredHoverProvider();
		String beansJSON = new String(Files.readAllBytes(new File(directory, "wrong-runtime-bean-information.json").toPath()));

		Range hint = provider.getLiveHoverHint((Annotation)node, document, beansJSON);
		assertNull(hint);
	}

	private TextDocument createTempTextDocument(String docURI) throws Exception {
		Path path = Paths.get(new URI(docURI));
		String content = new String(Files.readAllBytes(path));

		TextDocument doc = new TextDocument(docURI, LanguageId.PLAINTEXT, 0, content);
		return doc;
	}

	private CompilationUnit parse(TextDocument document, IJavaProject project)
			throws Exception, BadLocationException {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);

		String[] classpathEntries = getClasspathEntries(project);
		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		String docURI = document.getUri();
		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		parser.setSource(document.get(0, document.getLength()).toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		return cu;
	}

	private String[] getClasspathEntries(IJavaProject project) throws Exception {
		IClasspath classpath = project.getClasspath();
		Stream<Path> classpathEntries = classpath.getClasspathEntries();
		return classpathEntries
				.filter(path -> path.toFile().exists())
				.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
	}


}
