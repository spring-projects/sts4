/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.pom.tests;

import static org.junit.Assert.assertEquals;
import static org.springframework.ide.eclipse.maven.pom.PomDocumentDiffer.ignorePath;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;
import org.springframework.ide.eclipse.maven.pom.PomDocumentDiffer;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer.Difference;
import org.springframework.ide.eclipse.maven.pom.XmlDocumentDiffer.Direction;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class DifferencerTest {
	
	private IDocument leftDoc;
	private IDocument rightDoc;
	
	@Test
	public void smokeTest() {
		String xml1 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <parent>\n" + 
				"    <artifactId>eclipse.platform.team</artifactId>\n" + 
				"    <groupId>eclipse.platform.team</groupId>\n" + 
				"    <version>4.15.0-SNAPSHOT</version>\n" + 
				"    <relativePath>../../</relativePath>\n" + 
				"  </parent>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <version>3.4.800-SNAPSHOT</version>\n" + 
				"  <packaging>eclipse-plugin</packaging>\n" + 
				"</project>";
		String xml2 = xml1;
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffsAndGaps(differences
			// None
		);
	}

	@Test
	public void simpleTextDiff() {
		String xml1 = "<project>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"</project>";
		String xml2 = "<project>\n" + 
				"  <modelVersion>5.0.0</modelVersion>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffsAndGaps(differences,
			gap("<project>\n"),
			dif("  <modelVersion>4.0.0</modelVersion>\n", "  <modelVersion>5.0.0</modelVersion>\n"),
			gap("</project>")
		);
	}
	
	@Test
	public void reorderedProperties() throws Exception {
		String xml1 = "<project>\n" + 
				"  <whatever>\n" +
				"    <bar>2</bar>\n" +
				"    <bar>1</bar>\n" +
				"  </whatever>\n" + 
				"  <properties>\n" + 
				"    <start-class>demo.EmptyBootProjectApplication</start-class>\n" + 
				"    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" + 
				"    <java.version>1.7</java.version>\n" + 
				"  </properties>" + 
				"</project>";
		String xml2 = "<project>\n" + 
				"  <whatever>\n" +
				"    <bar>1</bar>\n" +
				"    <bar>2</bar>\n" +
				"  </whatever>\n" + 
				"  <properties>\n" + 
				"    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" + 
				"    <start-class>demo.EmptyBootProjectApplication</start-class>\n" + 
				"    <java.version>1.7</java.version>\n" + 
				"  </properties>" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences,
			dif("    <bar>2</bar>\n", "    <bar>1</bar>\n"),
			dif("    <bar>1</bar>\n", "    <bar>2</bar>\n")
		);
	}

	@Test
	public void reorderedProjectAttributes() throws Exception {
		String xml1 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <version>3.4.800-SNAPSHOT</version>\n" + 
				"  <packaging>eclipse-plugin</packaging>\n" +
				"</project>";
		String xml2 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <packaging>eclipse-plugin</packaging>\n" + 
				"  <version>3.4.800-SNAPSHOT</version>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffsAndGaps(differences
			// None
		);
	}
	
	@Test
	public void dependecniesReordered() throws Exception {
		String xml1 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		String xml2 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffsAndGaps(differences
			// None
		);
	}
	
	@Test
	public void dependencyAdded() throws Exception {
		String xml1 = "<project>\n" +
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		String xml2 = "<project>\n" + 
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences,
			lft("    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"    </dependency>\n")
		);
	}
	
	@Test
	public void dependencyRemoved() throws Exception {
		String xml1 = "<project>\n" +
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		String xml2 = "<project>\n" + 
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences,
			rgt("    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"    </dependency>\n")
		);
	}
	
	@Test
	public void dependencyAttributesReordered() throws Exception {
		String xml1 = "<project>\n" +
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		String xml2 = "<project>\n" + 
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences
			// none
		);
	}
	
	@Test
	public void dependencyChanged() throws Exception {
		String xml1 = "<project>\n" +
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"      <scope>compile</scope>\n" +
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		String xml2 = "<project>\n" + 
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences,
			rgt("      <scope>compile</scope>\n")
		);
	}
	
	@Test
	public void dependencyChangedInsideDependencyManagement() throws Exception {
		String xml1 = "<project>\n" +
				"  <dependencyManagement>\n" +
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"      <scope>compile</scope>\n" +
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"  </dependencyManagement>\n" +
				"</project>";
		String xml2 = "<project>\n" + 
				"  <dependencyManagement>\n" +
				"  <dependencies>\n" +
				"    <dependency>\n" + 
				"      <artifactId>spring-boot-starter-websocket</artifactId>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <groupId>org.springframework.cloud</groupId>\n" + 
				"      <artifactId>spring-cloud-function-web</artifactId>\n" + 
				"    </dependency>\n" + 
				"    <dependency>\n" + 
				"      <artifactId>spring-boot-starter-webflux</artifactId>\n" + 
				"      <groupId>org.springframework.boot</groupId>\n" + 
				"    </dependency>\n" + 
				"  </dependencies>\n" +
				"  </dependencyManagement>\n" +
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences,
			rgt("      <scope>compile</scope>\n")
		);
	}
	
	@Test
	public void shuffleReposAndAttributes() throws Exception {
		String xml1 = "<project>\n" +
				"	<repositories>\n" + 
				"		<repository>\n" + 
				"			<id>spring-snapshots</id>\n" + 
				"			<name>Spring Snapshots</name>\n" + 
				"			<url>https://repo.spring.io/snapshot</url>\n" + 
				"			<snapshots>\n" + 
				"				<enabled>true</enabled>\n" + 
				"			</snapshots>\n" + 
				"		</repository>\n" + 
				"		<repository>\n" + 
				"			<name>Spring Milestones</name>\n" + 
				"			<url>https://repo.spring.io/milestone</url>\n" + 
				"			<id>spring-milestones</id>\n" + 
				"		</repository>\n" + 
				"	</repositories>\n" + 
				"</project>";
		String xml2 = "<project>\n" + 
				"	<repositories>\n" + 
				"		<repository>\n" + 
				"			<id>spring-milestones</id>\n" + 
				"			<name>Spring Milestones</name>\n" + 
				"			<url>https://repo.spring.io/milestone</url>\n" + 
				"		</repository>\n" + 
				"		<repository>\n" + 
				"			<id>spring-snapshots</id>\n" + 
				"			<snapshots>\n" + 
				"				<enabled>true</enabled>\n" + 
				"			</snapshots>\n" + 
				"			<name>Spring Snapshots</name>\n" + 
				"			<url>https://repo.spring.io/snapshot</url>\n" + 
				"		</repository>\n" + 
				"	</repositories>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences
			// none
		);
	}
	
	@Test
	public void shufflePluginReposAndAttributes() throws Exception {
		String xml1 = "<project>\n" +
				"	<pluginRepositories>\n" + 
				"		<pluginRepository>\n" + 
				"			<id>spring-snapshots</id>\n" + 
				"			<name>Spring Snapshots</name>\n" + 
				"			<url>https://repo.spring.io/snapshot</url>\n" + 
				"			<snapshots>\n" + 
				"				<enabled>true</enabled>\n" + 
				"			</snapshots>\n" + 
				"		</pluginRepository>\n" + 
				"		<pluginRepository>\n" + 
				"			<name>Spring Milestones</name>\n" + 
				"			<url>https://repo.spring.io/milestone</url>\n" + 
				"			<id>spring-milestones</id>\n" + 
				"		</pluginRepository>\n" + 
				"	</pluginRepositories>\n" + 
				"</project>";
		String xml2 = "<project>\n" + 
				"	<pluginRepositories>\n" + 
				"		<pluginRepository>\n" + 
				"			<id>spring-milestones</id>\n" + 
				"			<name>Spring Milestones</name>\n" + 
				"			<url>https://repo.spring.io/milestone</url>\n" + 
				"		</pluginRepository>\n" + 
				"		<pluginRepository>\n" + 
				"			<id>spring-snapshots</id>\n" + 
				"			<snapshots>\n" + 
				"				<enabled>true</enabled>\n" + 
				"			</snapshots>\n" + 
				"			<name>Spring Snapshots</name>\n" + 
				"			<url>https://repo.spring.io/snapshot</url>\n" + 
				"		</pluginRepository>\n" + 
				"	</pluginRepositories>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences
			// none
		);
	}
	
	@Test
	public void shuffleBuildPluginsAndAttributes() throws Exception {
		String xml1 = "<project>\n" +
				"	<build>\n" + 
				"		<plugins>\n" + 
				"			<plugin>\n" + 
				"				<groupId>org.springframework.boot</groupId>\n" + 
				"				<artifactId>spring-boot-maven-plugin</artifactId>\n" + 
				"			</plugin>\n" + 
				"			<plugin>\n" + 
				"				<groupId>org.springframework.boot</groupId>\n" + 
				"				<artifactId>spring-boot-gradle-plugin</artifactId>\n" + 
				"			</plugin>\n" + 
				"		</plugins>\n" + 
				"	</build>\n" + 
				"</project>";
		String xml2 = "<project>\n" + 
				"	<build>\n" + 
				"		<plugins>\n" + 
				"			<plugin>\n" + 
				"				<artifactId>spring-boot-gradle-plugin</artifactId>\n" + 
				"				<groupId>org.springframework.boot</groupId>\n" + 
				"			</plugin>\n" + 
				"			<plugin>\n" + 
				"				<artifactId>spring-boot-maven-plugin</artifactId>\n" + 
				"				<groupId>org.springframework.boot</groupId>\n" + 
				"			</plugin>\n" + 
				"		</plugins>\n" + 
				"	</build>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences
			// none
		);
	}
	
	@Test
	public void shuffleExclusionsAndAttributes() throws Exception {
		String xml1 = "<project>\n" +
				"		<dependency>\n" + 
				"			<groupId>org.springframework.boot</groupId>\n" + 
				"			<artifactId>spring-boot-starter-test</artifactId>\n" + 
				"			<scope>test</scope>\n" + 
				"			<exclusions>\n" + 
				"				<exclusion>\n" + 
				"					<groupId>org.junit.vintage</groupId>\n" + 
				"					<artifactId>junit-vintage-engine</artifactId>\n" + 
				"				</exclusion>\n" + 
				"				<exclusion>\n" + 
				"					<groupId>org.junit</groupId>\n" + 
				"					<artifactId>org.junit5.jupiter</artifactId>\n" + 
				"				</exclusion>\n" + 
				"			</exclusions>\n" + 
				"		</dependency>\n" + 
				"</project>";
		String xml2 = "<project>\n" + 
				"		<dependency>\n" + 
				"			<groupId>org.springframework.boot</groupId>\n" + 
				"			<artifactId>spring-boot-starter-test</artifactId>\n" + 
				"			<scope>test</scope>\n" + 
				"			<exclusions>\n" + 
				"				<exclusion>\n" + 
				"					<artifactId>org.junit5.jupiter</artifactId>\n" + 
				"					<groupId>org.junit</groupId>\n" + 
				"				</exclusion>\n" + 
				"				<exclusion>\n" + 
				"					<artifactId>junit-vintage-engine</artifactId>\n" + 
				"					<groupId>org.junit.vintage</groupId>\n" + 
				"				</exclusion>\n" + 
				"			</exclusions>\n" + 
				"		</dependency>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		assertDiffs(differences
			// none
		);
	}
	
	@Test
	public void shuffleNonDependencyExclusionsAndAttributes() throws Exception {
		String xml1 = "<project>\n" +
				"		<something>\n" + 
				"			<exclusions>\n" + 
				"				<exclusion>\n" + 
				"					<groupId>org.junit.vintage</groupId>\n" + 
				"					<artifactId>junit-vintage-engine</artifactId>\n" + 
				"				</exclusion>\n" + 
				"				<exclusion>\n" + 
				"					<groupId>org.junit</groupId>\n" + 
				"					<artifactId>org.junit5.jupiter</artifactId>\n" + 
				"				</exclusion>\n" + 
				"			</exclusions>\n" + 
				"		</something>\n" + 
				"</project>";
		String xml2 = "<project>\n" + 
				"		<something>\n" + 
				"			<exclusions>\n" + 
				"				<exclusion>\n" + 
				"					<artifactId>junit-vintage-engine</artifactId>\n" + 
				"					<groupId>org.junit.vintage</groupId>\n" + 
				"				</exclusion>\n" + 
				"				<exclusion>\n" + 
				"					<groupId>org.junit</groupId>\n" + 
				"					<artifactId>org.junit5.jupiter</artifactId>\n" + 
				"				</exclusion>\n" + 
				"			</exclusions>\n" + 
				"		</something>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		printDifferences(differences);
		assertDiffs(differences,
			rgt("					<groupId>org.junit.vintage</groupId>\n"),
			rgt("					<artifactId>junit-vintage-engine</artifactId>\n"),
			lft("					<artifactId>junit-vintage-engine</artifactId>\n"),
			lft("					<groupId>org.junit.vintage</groupId>\n")
		);
	}
	
	@Test
	public void ignoreRelativePath() {
		String xml1 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <parent>\n" + 
				"    <artifactId>eclipse.platform.team</artifactId>\n" + 
				"    <groupId>eclipse.platform.team</groupId>\n" + 
				"    <version>4.15.0-SNAPSHOT</version>\n" + 
				"    <relativePath>../../</relativePath>\n" + 
				"  </parent>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <version>3.4.800-SNAPSHOT</version>\n" + 
				"  <packaging>eclipse-plugin</packaging>\n" + 
				"</project>";
		String xml2 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <parent>\n" + 
				"    <artifactId>eclipse.platform.team</artifactId>\n" + 
				"    <groupId>eclipse.platform.team</groupId>\n" + 
				"    <version>4.15.0-SNAPSHOT</version>\n" + 
				"  </parent>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <version>3.4.800-SNAPSHOT</version>\n" + 
				"  <packaging>eclipse-plugin</packaging>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2, differ -> differ.filter(ignorePath("project", "parent", "relativePath")));
		assertDiffs(differences
			// None
		);
	}
	
	@Test
	public void ignoreNameAndDescription() {
		String xml1 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <parent>\n" + 
				"    <artifactId>eclipse.platform.team</artifactId>\n" + 
				"    <groupId>eclipse.platform.team</groupId>\n" + 
				"    <version>4.15.0-SNAPSHOT</version>\n" + 
				"    <relativePath>../../</relativePath>\n" + 
				"  </parent>\n" + 
				"  <name>Compare UI</name>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <version>3.4.800-SNAPSHOT</version>\n" + 
				"  <packaging>eclipse-plugin</packaging>\n" + 
				"  <description>Eclipse plugin for Compare UI</description>\n" + 
				"</project>";
		String xml2 = "<project xmlns=\"https://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" + 
				"  <modelVersion>4.0.0</modelVersion>\n" + 
				"  <parent>\n" + 
				"    <artifactId>eclipse.platform.team</artifactId>\n" + 
				"    <groupId>eclipse.platform.team</groupId>\n" + 
				"    <version>4.15.0-SNAPSHOT</version>\n" + 
				"    <relativePath>../../</relativePath>\n" + 
				"  </parent>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <version>3.4.800-SNAPSHOT</version>\n" + 
				"  <packaging>eclipse-plugin</packaging>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2, differ -> differ.filter(ignorePath("project", "name").and(ignorePath("project", "description"))));
		assertDiffs(differences
			// None
		);
	}
	
	@Test
	public void pluginsDiff() throws Exception {
		String xml1 = "<project>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"	<build>\n" + 
				"  		<finalName>start-site</finalName>\n" +
				"		<plugins>\n" + 
				"			<plugin>\n" + 
				"				<groupId>org.apache.maven.plugins</groupId>\n" + 
				"				<artifactId>maven-jar-plugin</artifactId>\n" + 
				"			</plugin>\n" + 
				"		</plugins>\n" + 
				"	</build>\n" +
				"</project>";
		String xml2 = "<project>\n" +
				"	<build>\n" + 
				"		<plugins>\n" + 
				"			<plugin>\n" + 
				"				<groupId>org.apache.maven.plugins</groupId>\n" + 
				"				<artifactId>maven-jar-plugin</artifactId>\n" + 
				"			</plugin>\n" + 
				"		</plugins>\n" + 
				"	</build>\n" + 
				"  <artifactId>org.eclipse.compare.examples.xml</artifactId>\n" + 
				"  <groupId>org.eclipse.compare</groupId>\n" + 
				"</project>";
		
		List<Difference> differences = calculateDiffs(xml1, xml2);
		printDifferences(differences);
		assertDiffs(differences,
			rgt("  		<finalName>start-site</finalName>\n")
		);
	}
	
	private ExpectedDiff gap(String string) {
		return new ExpectedDiff(Direction.NONE, string, string);
	}

//	private ExpectedDiff gap(String left, String right) {
//		return new ExpectedDiff(Direction.NONE, left, right);
//	}
	
	private ExpectedDiff dif(String expectLeft, String expectRight) {
		return new ExpectedDiff(Direction.BOTH, expectLeft, expectRight);
	}

	private ExpectedDiff lft(String expectright) {
		return new ExpectedDiff(Direction.LEFT, "", expectright);
	}
	
	private ExpectedDiff rgt(String expectLeft) {
		return new ExpectedDiff(Direction.RIGHT, expectLeft, "");
	}
	
	private void assertDiffsAndGaps(List<Difference> actual, ExpectedDiff... expected) {
		assertEquals(expected.length, actual.size());
		int index = 0;
		for (ExpectedDiff expectedDiff : expected) {
			expectedDiff.assertMatches(actual.get(index++));
		}
	}
	
	private void printDifferences(List<Difference> differences) throws BadLocationException {
		for (Difference difference : differences) {
			if (difference.direction != Direction.NONE) {
				System.out.println("-------");
				System.out.println("Left: " + leftDoc.get(difference.leftRange.offset, difference.leftRange.length));
				System.out.println("Right: " + rightDoc.get(difference.rightRange.offset, difference.rightRange.length));
				System.out.println("-------");
			}
		}
	}
	
	private void assertDiffs(List<Difference> _actual, ExpectedDiff... expected) {
		List<Difference> actual = _actual.stream().filter(x -> x.direction != Direction.NONE).collect(Collectors.toList());
		assertEquals(expected.length, actual.size());
		int index = 0;
		for (ExpectedDiff expectedDiff : expected) {
			expectedDiff.assertMatches(actual.get(index++));
		}
	}

	private List<Difference> calculateDiffs(String xml1, String xml2) {
		XmlDocumentDiffer differ = PomDocumentDiffer.create(leftDoc = new Document(xml1), rightDoc = new Document(xml2));
		return differ.getDiffs();
	}
	
	private List<Difference> calculateDiffs(String xml1, String xml2, Consumer<XmlDocumentDiffer> configureDiffer) {
		XmlDocumentDiffer differ = PomDocumentDiffer.create(leftDoc = new Document(xml1), rightDoc = new Document(xml2));
		configureDiffer.accept(differ);
		return differ.getDiffs();
	}
	
	class ExpectedDiff {

		private String expectLeft;
		private String expectRight;
		private Direction expectDirection;

		public ExpectedDiff(Direction dir, String expectLeft, String expectRight) {
			this.expectDirection = dir;
			this.expectLeft = expectLeft;
			this.expectRight = expectRight;
		}

		public void assertMatches(Difference match) {
			assertEquals(expectDirection, match.direction);
			try {
				String leftText = leftDoc.get(match.leftRange.offset, match.leftRange.length);
				assertEquals("Left side difference text does not match!", expectLeft, leftText);
				String rightText = rightDoc.get(match.rightRange.offset, match.rightRange.length);
				assertEquals("Right side difference text does not match!", expectRight, rightText);
			} catch (BadLocationException e) {
				throw ExceptionUtil.unchecked(e);
			}
		}
		
	}
	
}
