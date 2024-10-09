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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.RecipeRun;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.maven.MavenParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.copilot.util.SpringCliException;
import org.springframework.util.StringUtils;

public abstract class AbstractInjectMavenActionHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractInjectMavenActionHandler.class);

	protected final TemplateEngine templateEngine;

	protected final Map<String, Object> model;

	protected final Path cwd;

	public AbstractInjectMavenActionHandler(TemplateEngine templateEngine, Map<String, Object> model, Path cwd) {
		this.templateEngine = templateEngine;
		this.model = model;
		this.cwd = cwd;
	}

	protected static ExecutionContext getExecutionContext() {
		Consumer<Throwable> onError = e -> {
			logger.error("error in javaParser execution", e);
		};
		return new InMemoryExecutionContext(onError);
	}

	protected String getTextToUse(String text, String actionName) {
		if (!StringUtils.hasText(text)) {
			throw new SpringCliException(actionName + " action does not have a value in the 'text:' field.");
		}
		if (this.templateEngine != null) {
			text = this.templateEngine.process(text, model);
		}
		return text;
	}

	@NotNull
	protected Path getPomPath() {
		Path pomPath = cwd.resolve("pom.xml");
		if (Files.notExists(pomPath)) {
			throw new SpringCliException("Could not find pom.xml in " + this.cwd
					+ ".  Make sure you are running the command in the directory that contains a pom.xml file");
		}
		return pomPath;
	}

	public void exec() {
		Path pomPath = getPomPath();
		List<Result> resultList = run().getChangeset().getAllResults();
		try {
			for (Result result : resultList) {
				// write updated file.
				try (BufferedWriter sourceFileWriter = Files.newBufferedWriter(pomPath, StandardCharsets.UTF_8)) {
					sourceFileWriter.write(result.getAfter().printAllTrimmed());
				}
			}
		} catch (IOException ex) {
			throw new SpringCliException("Error writing to " + pomPath.toAbsolutePath(), ex);
		}
	}

	public RecipeRun run() {
		List<Path> paths = new ArrayList<>();
		paths.add(getPomPath());
		MavenParser mavenParser = MavenParser.builder().build();
		List<SourceFile> parsedPomFiles = mavenParser.parse(paths, null, getExecutionContext()).toList();
		return createRecipe().run(new InMemoryLargeSourceSet(parsedPomFiles), getExecutionContext());
	}

	protected abstract Recipe createRecipe();

}
