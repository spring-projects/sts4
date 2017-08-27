package org.springframework.ide.vscode.boot.java.symbols;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.WorkspaceSymbolHandler;

public class BootJavaWorkspaceSymbolHandler implements WorkspaceSymbolHandler {

	private SimpleLanguageServer server;
	private JavaProjectFinder projectFinder;

	public BootJavaWorkspaceSymbolHandler(BootJavaLanguageServer server, JavaProjectFinder projectFinder) {
		this.server = server;
		this.projectFinder = projectFinder;
	}

	@Override
	public List<? extends SymbolInformation> handle(WorkspaceSymbolParams params) {
		Path root = this.server.getWorkspaceRoot();

		scanFiles(root.toFile());
		return collectSymbols();
	}

	private List<? extends SymbolInformation> collectSymbols() {
		return SimpleTextDocumentService.NO_SYMBOLS;
	}

	private void scanFiles(File directory) {
		if (this.projectFinder.isProjectRoot(directory)) {
			IJavaProject project = this.projectFinder.find(directory);
			if (project != null) {
				scanProject(project, directory);
			}
		}
		else if (directory.isDirectory() && directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				scanFiles(file);
			}
		}
	}

	private void scanProject(IJavaProject project, File directory) {
		try {
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

			scanFiles(parser, directory);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scanFiles(ASTParser parser, File directory) throws Exception {
		File[] javaFiles = directory.listFiles((file) -> file.getName().endsWith(".java"));
		for (File javaFile : javaFiles) {
			if (javaFile.isFile() && javaFile.exists()) {
				scanFile(parser, javaFile);
			}
		}

		File[] directories = directory.listFiles((file) -> file.isDirectory() && file.exists());
		for (File dir : directories) {
			scanFiles(parser, dir);
		}
	}

	private void scanFile(ASTParser parser, File javaFile) throws Exception {

		Path path = javaFile.toPath();

		String unitName = path.getFileName().toString();
		parser.setUnitName(unitName);

		String content = new String(Files.readAllBytes(path));
		parser.setSource(content.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		if (cu != null) {
			System.out.println("AST node found: " + cu.getClass().getName());
			scanAST(cu);
		}
	}

	private void scanAST(CompilationUnit cu) {
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				System.out.println("annotation found: " + node.toString());
				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				System.out.println("annotation found: " + node.toString());
				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				System.out.println("annotation found: " + node.toString());
				return super.visit(node);
			}
		});
	}

	private String[] getClasspathEntries(IJavaProject project) throws Exception {
		IClasspath classpath = project.getClasspath();
		Stream<Path> classpathEntries = classpath.getClasspathEntries();
		return classpathEntries
				.filter(path -> path.toFile().exists())
				.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
	}

}
