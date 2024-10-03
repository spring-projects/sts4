package org.springframework.ide.vscode.boot.java.copilot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.ChangeAnnotation;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.openrewrite.Result;
import org.openrewrite.xml.tree.Xml;
import org.springframework.ide.vscode.boot.java.copilot.InjectMavenActionHandler.MavenDependencyMetadata;
import org.springframework.ide.vscode.boot.java.copilot.util.ClassNameExtractor;
import org.springframework.ide.vscode.boot.java.copilot.util.PropertyFileUtils;
import org.springframework.ide.vscode.boot.java.copilot.util.SpringCliException;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.rewrite.ORDocUtils;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class ProjectArtifactEditGenerator {

	private final List<ProjectArtifact> projectArtifacts;

	private final Path projectPath;

	private final SimpleTextDocumentService simpleTextDocumentService;

	public ProjectArtifactEditGenerator(SimpleTextDocumentService simpleTextDocumentService,
			List<ProjectArtifact> projectArtifacts, Path projectPath, String readmeFileName) {
		this.simpleTextDocumentService = simpleTextDocumentService;
		this.projectArtifacts = projectArtifacts;
		this.projectPath = projectPath;
	}

	public ProcessArtifactResult<WorkspaceEdit> process() throws IOException {
		return processArtifacts(projectArtifacts, projectPath);
	}

	private ProcessArtifactResult<WorkspaceEdit> processArtifacts(List<ProjectArtifact> projectArtifacts,
			Path projectPath) throws IOException {
		ProcessArtifactResult<WorkspaceEdit> processArtifactResult = new ProcessArtifactResult<>();
		String changeAnnotationId = UUID.randomUUID().toString();
		WorkspaceEdit we = new WorkspaceEdit();
		ChangeAnnotation changeAnnotation = new ChangeAnnotation();
		changeAnnotation.setNeedsConfirmation(true);
		we.setDocumentChanges(new ArrayList<>());
		we.setChangeAnnotations(Map.of(changeAnnotationId, changeAnnotation));
		for (ProjectArtifact projectArtifact : projectArtifacts) {
			ProjectArtifactType artifactType = projectArtifact.getArtifactType();
			switch (artifactType) {
			case SOURCE_CODE:
				writeSourceCode(projectArtifact, projectPath, changeAnnotationId, we);
				break;
			case TEST_CODE:
				writeTestCode(projectArtifact, projectPath, changeAnnotationId, we);
				break;
			case MAVEN_DEPENDENCIES:
				writeMavenDependencies(projectArtifact, projectPath, changeAnnotationId, we);
				break;
			case APPLICATION_PROPERTIES:
				writeApplicationProperties(projectArtifact, projectPath, changeAnnotationId, we);
				break;
			case MAIN_CLASS:
				updateMainApplicationClassAnnotations(projectArtifact, projectPath, changeAnnotationId, we);
				break;
			case HTML:
				writeHtml(projectArtifact, projectPath, changeAnnotationId, we);
				break;
			default:
				processArtifactResult.addToNotProcessed(projectArtifact);
				break;
			}
		}
		processArtifactResult.setResult(we);
		return processArtifactResult;
	}

	private void writeSourceCode(ProjectArtifact projectArtifact, Path projectPath, String changeAnnotationId,
			WorkspaceEdit we) throws IOException {
		String packageName = this.calculatePackageForArtifact(projectArtifact);
		ClassNameExtractor classNameExtractor = new ClassNameExtractor();
		Optional<String> className = classNameExtractor.extractClassName(projectArtifact.getText());
		if (className.isPresent()) {
			Path output = resolveSourceFile(projectPath, packageName, className.get() + ".java");
			ORDocUtils.createWorkspaceEdit(simpleTextDocumentService, output.toUri().toASCIIString(),
					getFileContent(output), projectArtifact.getText(), changeAnnotationId, we);
		}
	}

	private String getFileContent(Path file) throws IOException {
		TextDocument doc = simpleTextDocumentService.getLatestSnapshot(file.toUri().toASCIIString());
		if (doc != null) {
			return doc.get();
		}
		return null;
	}

	private void writeTestCode(ProjectArtifact projectArtifact, Path projectPath, String changeAnnotationId,
			WorkspaceEdit we) throws IOException {
		// TODO parameterize better to reduce code duplication
		String packageName = this.calculatePackageForArtifact(projectArtifact);
		ClassNameExtractor classNameExtractor = new ClassNameExtractor();
		Optional<String> className = classNameExtractor.extractClassName(projectArtifact.getText());
		if (className.isPresent()) {
			Path output = resolveTestFile(projectPath, packageName, className.get() + ".java");
			ORDocUtils.createWorkspaceEdit(simpleTextDocumentService, output.toUri().toASCIIString(),
					getFileContent(output), projectArtifact.getText(), changeAnnotationId, we);
		}
	}

	private void writeMavenDependencies(ProjectArtifact projectArtifact, Path projectPath, String changeAnnotationId,
			WorkspaceEdit we) {
		Path currentProjectPomPath = this.projectPath.resolve("pom.xml");
		if (Files.notExists(currentProjectPomPath)) {
			throw new SpringCliException("Could not find pom.xml in " + this.projectPath
					+ ".  Make sure you are running the command in the project's root directory.");
		}

		InjectMavenActionHandler injectMavenActionHandler = new InjectMavenActionHandler(null, new HashMap<>(),
				projectPath);
		// Move the parsing to injectMavenActionHandler
		List<Xml.Document> xmlDocuments = injectMavenActionHandler.parseToXml(projectArtifact.getText());
		for (Xml.Document xmlDocument : xmlDocuments) {
			for (MavenDependencyMetadata dep : injectMavenActionHandler.findMavenDependencyTags(xmlDocument)) {
				injectMavenActionHandler.injectDependency(dep);
			}
		}

		List<Result> res = injectMavenActionHandler.run().getChangeset().getAllResults();
		if (!res.isEmpty()) {
			WorkspaceEdit workspaceEdit = ORDocUtils
					.createWorkspaceEdit(simpleTextDocumentService, res, changeAnnotationId).get();
			we.getDocumentChanges().addAll(workspaceEdit.getDocumentChanges());
		}
	}

	private void writeApplicationProperties(ProjectArtifact projectArtifact, Path projectPath,
			String changeAnnotationId, WorkspaceEdit we) throws IOException {
		Path applicationPropertiesPath = projectPath.resolve("src").resolve("main").resolve("resources")
				.resolve("application.properties");

		Properties srcProperties = new Properties();
		Properties destProperties = new Properties();
		srcProperties.load(IOUtils.toInputStream(projectArtifact.getText(), StandardCharsets.UTF_8));
		if (Files.exists(applicationPropertiesPath)) {
			destProperties.load(new FileInputStream(applicationPropertiesPath.toFile()));
		}
		Properties mergedProperties = PropertyFileUtils.mergeProperties(srcProperties, destProperties);

		StringWriter sw = new StringWriter();
		mergedProperties.store(sw, "updated by spring ai add");
		sw.flush();
		String newContent = sw.getBuffer().toString();
		ORDocUtils.createWorkspaceEdit(simpleTextDocumentService, applicationPropertiesPath.toUri().toASCIIString(),
				getFileContent(applicationPropertiesPath), newContent, changeAnnotationId, we);
	}

	private void updateMainApplicationClassAnnotations(ProjectArtifact projectArtifact, Path projectPath,
			String changeAnnotationId, WorkspaceEdit we) {
		// TODO mer
//		return Collections.emptyList();
	}

	private void writeHtml(ProjectArtifact projectArtifact, Path projectPath, String changeAnnotationId,
			WorkspaceEdit we) throws IOException {
		String html = projectArtifact.getText();
		String fileName = extractFilenameFromComment(html);
		if (fileName != null) {
			Path htmlFile = projectPath.resolve(fileName);
			ORDocUtils.createWorkspaceEdit(simpleTextDocumentService, fileName, getFileContent(htmlFile),
					projectArtifact.getText(), changeAnnotationId, we);
		}
	}

	private String calculatePackageForArtifact(ProjectArtifact projectArtifact) {
		String packageToUse = "com.example.ai";
		try (BufferedReader reader = new BufferedReader(new StringReader(projectArtifact.getText()))) {
			String firstLine = reader.readLine();
			if (firstLine.contains("package")) {
				String regex = "^package\\s+([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*);";
				Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
				Matcher matcher = pattern.matcher(firstLine);
				// Find the package statement and extract the package name
				String packageName = "";
				if (matcher.find()) {
					packageToUse = matcher.group(1);
				}
			}
		} catch (IOException ex) {
			throw new SpringCliException(
					"Could not parse package name from Project Artifact: " + projectArtifact.getText(), ex);
		}
		return packageToUse;
	}

	private static String extractFilenameFromComment(String content) {
		String commentPattern = "<!--\\s*filename:\\s*(\\S+\\.html)\\s*-->";
		Pattern pattern = Pattern.compile(commentPattern);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public Path resolveSourceFile(Path projectPath, String packageName, String fileName) {
		Path sourceDirectory = projectPath.resolve("src").resolve("main").resolve("java");
		return resolvePackage(sourceDirectory, packageName).resolve(fileName);
	}

	public Path resolveTestFile(Path projectPath, String packageName, String fileName) {
		Path sourceDirectory = projectPath.resolve("src").resolve("test").resolve("java");
		return resolvePackage(sourceDirectory, packageName).resolve(fileName);
	}

	private static Path resolvePackage(Path directory, String packageName) {
		return directory.resolve(packageName.replace('.', '/'));
	}

}
