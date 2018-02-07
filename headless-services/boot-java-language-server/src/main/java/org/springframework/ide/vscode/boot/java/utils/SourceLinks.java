/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient;
import org.springframework.ide.vscode.commons.util.Log;

/**
 * Utility for creating code navigate links for Markdown format documentation.
 * The utility looks at the environment variable "sts.lsp.client" to create a URL supported on a specific client
 *
 * @author Alex Boyko
 *
 */
public class SourceLinks {

	private static final String JAR = ".jar";
	private static final String JAVA = ".java";
	private static final String CLASS = ".class";

	public static Optional<String> sourceLinkUrl(IJavaProject project, String fqName) {
		switch (LspClient.currentClient()) {
		case VSCODE:
			return createVSCodeSourceLink(project, fqName);
		default:
			return Optional.empty();
		}
	}

	private static Optional<String> createVSCodeSourceLink(IJavaProject project, String fqName) {
		Optional<File> classpathResource = project.getClasspath().findClasspathResourceContainer(fqName);
		if (classpathResource.isPresent()) {
			File file = classpathResource.get();
			if (file.isDirectory()) {
				return javaSourceLinkUrl(project, fqName, file);
			} else if (file.getName().endsWith(JAR)) {
				return jarSourceLinkUrl(project, fqName, file);
			}
		}
		return Optional.empty();
	}

	private static Optional<String> javaSourceLinkUrl(IJavaProject project, String javaRelativePath, String fqName) {
		Path path = Paths.get(javaRelativePath);
		Optional<Path> sourceResource = project.getClasspath().getSourceFolders().stream()
				.map(r -> Paths.get(r).resolve(path)).filter(r -> Files.exists(r)).findFirst();
		if (sourceResource.isPresent()) {
			return Optional.of(sourceResource.get().toUri().toString());
		}
		return Optional.empty();
	}

	private static Optional<String> javaSourceLinkUrl(IJavaProject project, String fqName, File containerFolder) {
		IClasspath classpath = project.getClasspath();
		if (containerFolder.toPath().startsWith(classpath.getOutputFolder())) {
			int innerTypeIdx = fqName.indexOf('$');
			String topLevelType = innerTypeIdx > 0 ? fqName.substring(0, innerTypeIdx) : fqName;
			return javaSourceLinkUrl(project, topLevelType.replaceAll("\\.", "/") + JAVA, fqName);
		}
		return Optional.empty();
	}

	private static Optional<String> jarSourceLinkUrl(IJavaProject project, String fqName, File jarFile) {
		try {
			int lastDotIndex = fqName.lastIndexOf('.');
			String packageName = fqName.substring(0, lastDotIndex);
			String typeName = fqName.substring(lastDotIndex + 1);
			String jarFileName = jarFile.getName();
			StringBuilder sb = new StringBuilder();
			sb.append("jdt://contents/");
			sb.append(jarFileName);
			sb.append("/");
			sb.append(packageName);
			sb.append("/");
			sb.append(typeName);
			sb.append(CLASS);
			sb.append("?");

			StringBuilder query = new StringBuilder();
			query.append("=");
			query.append(project.getElementName());
			query.append("/");
			String convertedPath = String.join("\\/", jarFile.toString().replaceAll("\\\\", "/").split("/"));
			query.append(convertedPath);
			query.append("<");
			query.append(packageName);
			query.append("(");
			query.append(typeName);
			query.append(CLASS);

			sb.append(URLEncoder.encode(query.toString(), "UTF8"));

			return Optional.of(sb.toString());
		} catch (Throwable t) {
			Log.log(t);
		}
		return Optional.empty();
	}

//	private static Optional<Region> findTypeRange(CompilationUnit cu, String fqName) {
//		int[] values = new int[] {0, -1};
//		int lastDotIndex = fqName.lastIndexOf('.');
//		String packageName = fqName.substring(0, lastDotIndex);
//		String typeName = fqName.substring(lastDotIndex + 1);
//		if (packageName.equals(cu.getPackage().getName().getFullyQualifiedName())) {
//			Stack<String> visitedType = new Stack<>();
//			cu.accept(new ASTVisitor() {
//
//				@Override
//				public boolean visit(TypeDeclaration node) {
//					if (values[1] < 0) {
//						visitedType.push(node.getName().getIdentifier());
//						if (String.join("$", visitedType.toArray(new String[visitedType.size()])).equals(typeName)) {
//							values[0] = node.getName().getStartPosition();
//							values[1] = node.getName().getLength();
//						}
//					}
//					return values[1] < 0;
//				}
//
//				@Override
//				public void endVisit(TypeDeclaration node) {
//					visitedType.pop();
//					super.endVisit(node);
//				}
//
//			});
//		}
//		return Optional.ofNullable(values[1] < 0 ? null : new Region(values[0], values[1]));
//	}

}
