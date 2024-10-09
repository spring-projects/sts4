/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.copilot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.ChangeAnnotation;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Result;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.spring.AddSpringProperty;
import org.openrewrite.java.spring.ChangeSpringPropertyValue;
import org.openrewrite.properties.PropertiesParser;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.yaml.YamlParser;
import org.springframework.ide.vscode.boot.java.copilot.InjectMavenActionHandler.MavenDependencyMetadata;
import org.springframework.ide.vscode.boot.java.copilot.util.ClassNameExtractor;
import org.springframework.ide.vscode.boot.java.copilot.util.SpringCliException;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.ide.vscode.commons.rewrite.ORDocUtils;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class ProjectArtifactEditGenerator {

	private final List<ProjectArtifact> projectArtifacts;

	private final IJavaProject project;

	private final SimpleTextDocumentService simpleTextDocumentService;

	public ProjectArtifactEditGenerator(SimpleTextDocumentService simpleTextDocumentService,
			List<ProjectArtifact> projectArtifacts, IJavaProject project, String readmeFileName) {
		this.simpleTextDocumentService = simpleTextDocumentService;
		this.projectArtifacts = projectArtifacts;
		this.project = project;
	}

	public ProcessArtifactResult<WorkspaceEdit> process() throws Exception {
		return processArtifacts(projectArtifacts, Paths.get(project.getLocationUri()));
	}

	private ProcessArtifactResult<WorkspaceEdit> processArtifacts(List<ProjectArtifact> projectArtifacts,
			Path projectPath) throws Exception {
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
			ORDocUtils.addToWorkspaceEdit(simpleTextDocumentService, output.toUri().toASCIIString(),
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
			ORDocUtils.addToWorkspaceEdit(simpleTextDocumentService, output.toUri().toASCIIString(),
					getFileContent(output), projectArtifact.getText(), changeAnnotationId, we);
		}
	}

	private void writeMavenDependencies(ProjectArtifact projectArtifact, Path projectPath, String changeAnnotationId,
			WorkspaceEdit we) {
		Path currentProjectPomPath = Paths.get(project.getLocationUri()).resolve("pom.xml");
		if (Files.notExists(currentProjectPomPath)) {
			throw new SpringCliException("Could not find pom.xml in " + Paths.get(project.getLocationUri())
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
			String changeAnnotationId, WorkspaceEdit we) throws Exception {
		
		List<Path> propFiles = new ArrayList<>();
		List<Path> yamlFiles = new ArrayList<>();
		AtomicReference<Path> sourceFolder = new AtomicReference<>();
			project.getClasspath().getClasspathEntries().stream().filter(Classpath::isSource)
				.filter(cpe -> !cpe.isSystem() && !cpe.isTest() && cpe.isOwn() && !cpe.isJavaContent())
				.map(cpe -> new File(cpe.getPath()).toPath())
				.flatMap(folder -> {
					sourceFolder.compareAndSet(null, folder);
					try {
						return Files.list(folder);
					} catch (IOException e) {
						return Stream.empty();
					}
				}).forEach(p -> {
					String fileName = p.getFileName().toString();
					if (propFiles.isEmpty() && "application.properties".equals(fileName)) {
						propFiles.add(p);
					} else if (yamlFiles.isEmpty() && ("application.yml".equals(fileName) || "application.yaml".equals(fileName))) {
						yamlFiles.add(p);
					}
				});

		if (propFiles.isEmpty() && yamlFiles.isEmpty()) {
			Path propsFile = (sourceFolder.get() == null ? Paths.get(project.getLocationUri()).resolve("src/main/resources") : sourceFolder.get()).resolve("application.properties");
			ORDocUtils.addToWorkspaceEdit(simpleTextDocumentService, propsFile.toUri().toASCIIString(),
					null, projectArtifact.getText(), changeAnnotationId, we);
		} else {
			DeclarativeRecipe aggregateRecipe = new DeclarativeRecipe("spring-tools.ai.PropertiesUpdates",
					"Add property files changes from AI", "", Collections.emptySet(), null, null, false, Collections.emptyList());
			Properties srcProperties = new Properties();
			srcProperties.load(IOUtils.toInputStream(projectArtifact.getText(), StandardCharsets.UTF_8));
			for (Map.Entry<?, ?> e : srcProperties.entrySet()) {
				aggregateRecipe.getRecipeList().add(new AddSpringProperty(e.getKey().toString(), e.getValue().toString(), null, List.of()));
				aggregateRecipe.getRecipeList().add(new ChangeSpringPropertyValue(e.getKey().toString(), e.getValue().toString(), null, null, null));
			}
			ORDocUtils.addToWorkspaceEdit(
					simpleTextDocumentService,
					aggregateRecipe.run(
							new InMemoryLargeSourceSet(PropertiesParser.builder().build().parse(propFiles, null, new InMemoryExecutionContext()).collect(Collectors.toList())),
							new InMemoryExecutionContext())
					.getChangeset().getAllResults(),
					changeAnnotationId,
					we);
			ORDocUtils.addToWorkspaceEdit(
					simpleTextDocumentService,
					aggregateRecipe.run(
							new InMemoryLargeSourceSet(YamlParser.builder().build().parse(yamlFiles, null, new InMemoryExecutionContext()).collect(Collectors.toList())),
							new InMemoryExecutionContext())
					.getChangeset().getAllResults(),
					changeAnnotationId,
					we);
		}
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
			ORDocUtils.addToWorkspaceEdit(simpleTextDocumentService, fileName, getFileContent(htmlFile),
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
//				String packageName = "";
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
