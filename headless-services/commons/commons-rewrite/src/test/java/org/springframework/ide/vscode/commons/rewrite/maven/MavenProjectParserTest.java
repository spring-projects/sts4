/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.SourceFile;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.marker.JavaSourceSet;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.maven.MavenParser;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.text.PlainText;
import org.openrewrite.yaml.tree.Yaml;

public class MavenProjectParserTest {
	
	@Test
	void parseResources() throws Exception {
		URL resource = getClass().getResource("/test-projects/test-maven-project-parser");
		Path testProjectPath = Paths.get(resource.toURI());
        MavenParser.Builder mavenParserBuilder = MavenParser.builder()
                .mavenConfig(testProjectPath.resolve(".mvn/maven.config"));

		MavenProjectParser parser = new MavenProjectParser(mavenParserBuilder, JavaParser.fromJavaVersion(), new InMemoryExecutionContext(), null);
		
		List<SourceFile> sources = parser.parse(testProjectPath, Collections.emptyList());
		
		assertEquals(12, sources.size());
		
		// java sources
		assertEquals(6, sources.stream().filter(JavaSourceFile.class::isInstance).collect(Collectors.toList()).size());
		
		// java sources from main
		assertEquals(5, sources.stream().filter(JavaSourceFile.class::isInstance).map(JavaSourceFile.class::cast).filter(j -> {
			String sourceSetName = j.getMarkers().findFirst(JavaSourceSet.class).map(js -> js.getName()).orElse(null);
			return MavenProjectParser.MAIN.equals(sourceSetName);
		}).collect(Collectors.toList()).size());

		// java sources from test
		assertEquals(1, sources.stream().filter(JavaSourceFile.class::isInstance).map(JavaSourceFile.class::cast).filter(j -> {
			String sourceSetName = j.getMarkers().findFirst(JavaSourceSet.class).map(js -> js.getName()).orElse(null);
			return MavenProjectParser.TEST.equals(sourceSetName);
		}).collect(Collectors.toList()).size());
		
		// properties files
		assertEquals(1, sources.stream().filter(Properties.File.class::isInstance).collect(Collectors.toList()).size());
		
		// application properties
		assertEquals(1, sources.stream().filter(Properties.File.class::isInstance).filter(f -> f.getSourcePath().getFileName().toString().equals("application.properties")).collect(Collectors.toList()).size());

		// yaml files
		assertEquals(2, sources.stream().filter(Yaml.Documents.class::isInstance).collect(Collectors.toList()).size());
		
		// application yaml
		assertEquals(2, sources.stream().filter(Yaml.Documents.class::isInstance).filter(f -> f.getSourcePath().getFileName().toString().equals("application.yml")).collect(Collectors.toList()).size());
		
		// plain text files
		assertEquals(2, sources.stream().filter(PlainText.class::isInstance).collect(Collectors.toList()).size());

		// .factories files
		assertEquals(2, sources.stream().filter(PlainText.class::isInstance).filter(f -> f.getSourcePath().getFileName().toString().endsWith(".factories")).collect(Collectors.toList()).size());

	}

}
