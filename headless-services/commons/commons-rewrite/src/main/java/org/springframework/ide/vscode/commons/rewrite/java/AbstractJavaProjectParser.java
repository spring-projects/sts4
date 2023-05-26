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
package org.springframework.ide.vscode.commons.rewrite.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openrewrite.Parser;
import org.openrewrite.Parser.Input;
import org.openrewrite.SourceFile;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaParser.Builder;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.ide.vscode.commons.protocol.java.Classpath.CPE;

public abstract class AbstractJavaProjectParser extends ProjectParser {
	
    private static String[] NON_JAVA_FILE_TYPES = new String[] { ".properties", ".xml", ".yml", ".yaml", ".factories" };

	private static String JAVA_FILE_TYPE = ".java";
	
	private static Predicate<CPE> MAIN_JAVA_CPE_FILTER = cpe -> Classpath.isSource(cpe) && cpe.isOwn() && cpe.isJavaContent() && !cpe.isTest();
	
	private static Predicate<CPE> TEST_JAVA_CPE_FILTER = cpe -> Classpath.isSource(cpe) && cpe.isOwn() && cpe.isJavaContent() && cpe.isTest();
	
	private static Predicate<CPE> MAIN_NON_JAVA_CPE_FILTER = cpe -> Classpath.isSource(cpe) && cpe.isOwn() && !cpe.isJavaContent() && !cpe.isTest();
	
	private static Predicate<CPE> TEST_NON_JAVA_CPE_FILTER = cpe -> Classpath.isSource(cpe) && cpe.isOwn() && !cpe.isJavaContent() && cpe.isTest();
	
	protected IJavaProject jp;
	private Builder<?, ?> javaParserBuilder;
	private Function<Path, Input> parserInputProvider;

	protected AbstractJavaProjectParser(IJavaProject jp, JavaParser.Builder<?, ?> javaParserBuilder,
			Function<Path, Parser.Input> parserInputProvider) {
		this.jp = jp;
		this.javaParserBuilder = javaParserBuilder;
		this.parserInputProvider = parserInputProvider;
	}

	
	@Override
	protected List<SourceSet> getSourceSets(Path projectDir, SourceFile buildFile) {
		return List.of(
				new SourceSet(
					ProjectParser.MAIN,
					getSources(MAIN_JAVA_CPE_FILTER, JAVA_FILE_TYPE),
					getSources(MAIN_NON_JAVA_CPE_FILTER, NON_JAVA_FILE_TYPES),
					IClasspathUtil.getBinaryRoots(jp.getClasspath(), cpe -> !cpe.isTest()).stream().map(f -> f.toPath()).collect(Collectors.toList())
				),
				new SourceSet(
					ProjectParser.TEST,
					getSources(TEST_JAVA_CPE_FILTER, JAVA_FILE_TYPE),
					getSources(TEST_NON_JAVA_CPE_FILTER, NON_JAVA_FILE_TYPES),
					IClasspathUtil.getBinaryRoots(jp.getClasspath(), cpe -> !cpe.isTest()).stream().map(f -> f.toPath()).collect(Collectors.toList())
				)
		);
	}


	private List<Path> getSources(Predicate<CPE> cpeFilter, String... fileTypes) {
		BiPredicate<Path, java.nio.file.attribute.BasicFileAttributes> predicate = (p, bfa) -> bfa.isRegularFile()
				&& Arrays.stream(fileTypes).anyMatch(type -> p.getFileName().toString().endsWith(type));

		try {
			return jp.getClasspath().getClasspathEntries().stream().filter(cpeFilter).flatMap(cpe -> {
				try {
					return Files.find(Paths.get(cpe.getPath()), 999, predicate);
				} catch (IOException e) {
					return Stream.empty();
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	protected Input createParserInput(Path p) {
		Parser.Input in = null;
		if (parserInputProvider != null) {
			in = parserInputProvider.apply(p);
		}
		if (in == null) {
			return super.createParserInput(p);
		}
		return in;
	}


	@Override
	protected Builder<?, ?> getJavaParserBuilder() {
		return javaParserBuilder;
	}

}
