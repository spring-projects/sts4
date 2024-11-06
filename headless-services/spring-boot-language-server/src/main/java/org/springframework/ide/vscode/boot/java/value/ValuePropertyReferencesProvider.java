/*******************************************************************************
 * Copyright (c) 2017, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value;

import static org.springframework.ide.vscode.commons.yaml.ast.NodeUtil.asScalar;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.ReferenceProvider;
import org.springframework.ide.vscode.boot.properties.BootPropertiesLanguageServerComponents;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.ParseResults;
import org.springframework.ide.vscode.java.properties.parser.Parser;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.KeyValuePair;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;

/**
 * @author Martin Lippert
 */
public class ValuePropertyReferencesProvider implements ReferenceProvider {
	
	private static final Logger log = LoggerFactory.getLogger(ValuePropertyReferencesProvider.class);

	private final JavaProjectFinder projectFinder;
	private final PropertyExtractor propertyExtractor;

	public ValuePropertyReferencesProvider(JavaProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
		this.propertyExtractor = new PropertyExtractor();
	}

	@Override
	public List<? extends Location> provideReferences(CancelChecker cancelToken, IJavaProject project, ASTNode node, Annotation annotation, ITypeBinding type, int offset) {
		
		cancelToken.checkCanceled();

		try {
			if (node instanceof StringLiteral) {
				String propertyKey = this.propertyExtractor.extractPropertyKey((StringLiteral) node);
				if (propertyKey != null) {
					return findReferencesFromPropertyFiles(propertyKey);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<? extends Location> findReferencesFromPropertyFiles(String propertyKey) {
		Collection<? extends IJavaProject> allProjects = this.projectFinder.all();
		
		try {
			return allProjects
				.stream()
				.flatMap(project -> IClasspathUtil.getSourceFolders(project.getClasspath()))
				.flatMap(sourceFolder -> {
					try {
						return Files.walk(sourceFolder.toPath());
					} catch (IOException e) {
						return Stream.empty();
					}
				})
				.filter(path -> ValuePropertyReferencesProvider.isPropertiesFile(path))
				.filter(path -> path.toFile().isFile())
				.map(path -> findReferences(path, propertyKey))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		}
		catch (Exception e) {
			return null;
		}
	}

	public static boolean isPropertiesFile(Path path) {
		String fileName = path.getFileName().toString();

		if (fileName.endsWith(BootPropertiesLanguageServerComponents.PROPERTIES)) {
			return fileName.contains("application");
		} else {
			for (String yml : BootPropertiesLanguageServerComponents.YML) {
				if (fileName.endsWith(yml)) {
					return fileName.contains("application");
				}
			}
		}
		return false;
	}

	public List<Location> findReferences(Path path, String propertyKey) {
		String filePath = path.toString();
		if (filePath.endsWith(BootPropertiesLanguageServerComponents.PROPERTIES)) {
			return findReferencesInPropertiesFile(path.toFile(), propertyKey);
		} else {
			for (String yml : BootPropertiesLanguageServerComponents.YML) {
				if (filePath.endsWith(yml)) {
					return findReferencesInYMLFile(path.toFile(), propertyKey);
				}
			}
		}
		return new ArrayList<Location>();
	}

	private List<Location> findReferencesInYMLFile(File file, String propertyKey) {
		
		return findReferencesInYMLFile(file, propertyKey, foundNodeTuple -> {
			Position start = new Position();
			Node foundNode = foundNodeTuple.getKeyNode();
			start.setLine(foundNode.getStartMark().getLine());
			start.setCharacter(foundNode.getStartMark().getColumn());

			Position end = new Position();
			end.setLine(foundNode.getEndMark().getLine());
			end.setCharacter(foundNode.getEndMark().getColumn());

			Range range = new Range();
			range.setStart(start);
			range.setEnd(end);

			return Optional.of(new Location(file.toPath().toUri().toASCIIString(), range));
		});
	}

	static List<Location> findReferencesInYMLFile(File file, String propertyKey, Function<NodeTuple, Optional<Location>> processor) {
		List<Location> foundLocations = new ArrayList<>();

		try {
			String fileContent = FileUtils.readFileToString(file, Charset.defaultCharset());

			YamlASTProvider parser = new YamlParser();

			URI docURI = file.toURI();
			TextDocument doc = new TextDocument(docURI.toASCIIString(), null);
			doc.setText(fileContent);
			YamlFileAST ast = parser.getAST(doc);

			List<Node> nodes = ast.getNodes();
			if (nodes != null && !nodes.isEmpty()) {
				for (Node node : nodes) {
					try {
						NodeTuple foundNodeTuple = findNode(node, "", propertyKey);
						if (foundNodeTuple != null) {
							processor.apply(foundNodeTuple).ifPresent(foundLocations::add);
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return foundLocations;
	}
	
	protected static NodeTuple findNode(Node node, String prefix, String propertyKey) {
		if (node.getNodeId().equals(NodeId.mapping)) {
			for (NodeTuple entry : ((MappingNode)node).getValue()) {
				Node keyNode = entry.getKeyNode();
				String key = asScalar(keyNode);

				String combinedKey = prefix.length() > 0 ? prefix + "." + key : key;

				if (combinedKey != null && combinedKey.equals(propertyKey)) {
					return entry;
				}
				else {
					NodeTuple recursive = findNode(entry.getValueNode(), combinedKey, propertyKey);
					if (recursive != null) {
						return recursive;
					}
				}
			}
		}

		return null;
	}

	private List<Location> findReferencesInPropertiesFile(File file, String propertyKey) {
		
		return findReferencesInPropertiesFile(file, propertyKey, (pair, doc) -> {
			try {
				int line = doc.getLineOfOffset(pair.getKey().getOffset());
				int startInLine = pair.getKey().getOffset() - doc.getLineOffset(line);
				int endInLine = startInLine + (pair.getKey().getLength());
	
				Position start = new Position();
				start.setLine(line);
				start.setCharacter(startInLine);
	
				Position end = new Position();
				end.setLine(line);
				end.setCharacter(endInLine);
	
				Range range = new Range();
				range.setStart(start);
				range.setEnd(end);
	
				return Optional.of(new Location(file.toPath().toUri().toASCIIString(), range));
			} catch (Exception e) {
				log.error("", e);
				return Optional.empty();
			}
		});
	}
	
	static List<Location> findReferencesInPropertiesFile(File file, String propertyKey, BiFunction<KeyValuePair, TextDocument, Optional<Location>> processor) {
		List<Location> foundLocations = new ArrayList<>();
		try {
			String fileContent = FileUtils.readFileToString(file, Charset.defaultCharset());
	
			Parser parser = new AntlrParser();
			ParseResults parseResults = parser.parse(fileContent);
	
			if (parseResults != null && parseResults.ast != null) {
				parseResults.ast.getPropertyValuePairs().forEach(pair -> {
					if (pair.getKey() != null && pair.getKey().decode().equals(propertyKey)) {
						TextDocument doc = new TextDocument(file.toURI().toASCIIString(), null);
						doc.setText(fileContent);
						try {
							processor.apply(pair, doc).ifPresent(foundLocations::add);
						} catch (Exception e) {
							log.error("", e);
						}
					}
				});
			}
		} catch (IOException e) {
			log.error("", e);
		}
		
		return foundLocations;
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
