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
package org.springframework.ide.vscode.boot.java.references;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class ValuePropertyReferencesProvider {
	
	private SimpleLanguageServer languageServer;

	public ValuePropertyReferencesProvider(SimpleLanguageServer languageServer) {
		this.languageServer = languageServer;
	}

	public CompletableFuture<List<? extends Location>> provideReferencesForValueAnnotation(ASTNode node, Annotation annotation,
			ITypeBinding type, int offset, TextDocument doc) {

		try {
			// case: @Value("prefix<*>")
			if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideReferences(node.toString(), offset - node.getStartPosition(), node.getStartPosition(), doc);
				}
			}
			// case: @Value(value="prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideReferences(node.toString(), offset - node.getStartPosition(), node.getStartPosition(), doc);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private CompletableFuture<List<? extends Location>> provideReferences(String value, int offset, int nodeStartOffset, TextDocument doc) {

		try {
			LocalRange range = getPropertyRange(value, offset);
			if (range != null) {
				String propertyKey = value.substring(range.getStart(), range.getEnd());
				if (propertyKey != null && propertyKey.length() > 0) {
					return findReferencesFromPropertyFiles(languageServer.getWorkspaceRoot(), propertyKey);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public CompletableFuture<List<? extends Location>> findReferencesFromPropertyFiles(Path workspaceRoot,
			String propertyKey) {
		
		try (Stream<Path> walk = Files.walk(workspaceRoot)) {
			List<Location> locations = walk
					.filter(path -> isPropertiesFile(path))
					.filter(path -> path.toFile().isFile())
					.map(path -> findReferences(path))
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
			
			return CompletableFuture.completedFuture(locations);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private boolean isPropertiesFile(Path path) {
		return path.toString().endsWith("application.properties");
	}

	private List<Location> findReferences(Path path) {
		String filePath = path.toString();
		if (filePath.endsWith(".properties")) {
			// do the real work
		}
		else if (filePath.endsWith(".yml")) {
			// do the real work
		}
		return new ArrayList<Location>();
	}

	public LocalRange getPropertyRange(String value, int offset) {
		int start = -1;
		int end = -1;

		for (int i = offset - 1; i >= 0; i--) {
			if (value.charAt(i) == '{') {
				start = i + 1;
				break;
			}
			else if (value.charAt(i) == '}') {
				break;
			}
		}

		for(int i = offset; i < value.length(); i++) {
			if (value.charAt(i) == '{' || value.charAt(i) == '$') {
				break;
			}
			else if (value.charAt(i) == '}') {
				end = i;
				break;
			}
		}

		if (start > 0 && start < value.length() && end > 0 && end <= value.length() && start < end) {
			return new LocalRange(start, end);
		}

		return null;
	}

	public static class LocalRange {
		private int start;
		private int end;

		public LocalRange(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

	}
}
