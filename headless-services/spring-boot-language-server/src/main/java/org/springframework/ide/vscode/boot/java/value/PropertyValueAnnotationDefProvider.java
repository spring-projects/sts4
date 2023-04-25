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
package org.springframework.ide.vscode.boot.java.value;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.IJavaDefinitionProvider;
import org.springframework.ide.vscode.boot.properties.BootPropertiesLanguageServerComponents;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.yaml.snakeyaml.nodes.Node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class PropertyValueAnnotationDefProvider implements IJavaDefinitionProvider {
	
	private static final Logger log = LoggerFactory.getLogger(PropertyValueAnnotationDefProvider.class);

	@Override
	public List<LocationLink> getDefinitions(CancelChecker cancelToken, IJavaProject project, CompilationUnit cu,
			ASTNode n) {
		if (n instanceof StringLiteral) {
			StringLiteral valueNode = (StringLiteral) n;
			String propertyKey = null;
			
			ASTNode parent = valueNode.getParent();
			if (parent instanceof Annotation && isApplicableValueAnnotation((Annotation) parent)) {
				propertyKey = extractPropertyKey(valueNode.getLiteralValue());
			} else if (parent instanceof MemberValuePair
					&& "value".equals(((MemberValuePair) parent).getName().getIdentifier())
					&& parent.getParent() instanceof Annotation
					&& isApplicableValueAnnotation((Annotation) parent.getParent())) {
				propertyKey = extractPropertyKey(valueNode.getLiteralValue());
			}
			
			if (propertyKey != null) {
				Builder<LocationLink> builder = ImmutableList.builder();
				Map<Location, Range> targetRanges = new HashMap<>();

				Position startPosition = new Position(cu.getLineNumber(valueNode.getStartPosition()) - 1,
						cu.getColumnNumber(valueNode.getStartPosition()));
				Position endPosition = new Position(
						cu.getLineNumber(valueNode.getStartPosition() + valueNode.getLength()) - 1,
						cu.getColumnNumber(valueNode.getStartPosition() + valueNode.getLength()));
				Range originRange = new Range(startPosition, endPosition);
				

				for (Location location : findValueReferences(project, propertyKey, targetRanges)) {
					LocationLink ll = new LocationLink();
					ll.setTargetUri(location.getUri());
					ll.setTargetSelectionRange(location.getRange());
					ll.setTargetRange(targetRanges.get(location));
					ll.setOriginSelectionRange(originRange);
					builder.add(ll);
				}
				return builder.build();
			}
		}
		return Collections.emptyList();
	}
	
	private static boolean isApplicableValueAnnotation(Annotation a) {
		IAnnotationBinding binding = a.resolveAnnotationBinding();
		return binding != null && Annotations.VALUE.equals(binding.getAnnotationType().getQualifiedName())
				&& a.getParent() instanceof FieldDeclaration;
	}
	
	private List<Location> findValueReferences(IJavaProject project, String propertyKey, Map<Location, Range> targetRanges) {
		Builder<Location> links = ImmutableList.builder();
		IClasspathUtil.getClasspathResourcesFullPaths(project.getClasspath()).forEach(path -> {
			if (ValuePropertyReferencesProvider.isPropertiesFile(path)) {
				String filePath = path.toString();
				if (filePath.endsWith(BootPropertiesLanguageServerComponents.PROPERTIES)) {
					links.addAll(ValuePropertyReferencesProvider.findReferencesInPropertiesFile(path.toFile(), propertyKey, (pair, doc) -> {
						try {
							int line = doc.getLineOfOffset(pair.getValue().getOffset());
							int startInLine = pair.getValue().getOffset() - doc.getLineOffset(line);
							int endInLine = startInLine + (pair.getValue().getLength());
				
							Position start = new Position();
							start.setLine(line);
							start.setCharacter(startInLine);
				
							Position end = new Position();
							end.setLine(line);
							end.setCharacter(endInLine);
				
							Range range = new Range();
							range.setStart(start);
							range.setEnd(end);
							
							Location location = new Location(path.toUri().toASCIIString(), range);
							
							targetRanges.put(location, doc.toRange(pair.getOffset(), pair.getLength()));
							
							return Optional.of(location);
						} catch (Exception e) {
							log.error("", e);
							return Optional.empty();
						}
					}));
				} else {
					for (String yml : BootPropertiesLanguageServerComponents.YML) {
						if (filePath.endsWith(yml)) {
							links.addAll(ValuePropertyReferencesProvider.findReferencesInYMLFile(path.toFile(), propertyKey, nodeTuple -> {
								// property key node is found. Get the value node
								Node valueNode = nodeTuple.getValueNode();
								Position valueStart = new Position();
								valueStart.setLine(valueNode.getStartMark().getLine());
								valueStart.setCharacter(valueNode.getStartMark().getColumn());

								Position valueEnd = new Position();
								valueEnd.setLine(valueNode.getEndMark().getLine());
								valueEnd.setCharacter(valueNode.getEndMark().getColumn());

								Range range = new Range();
								range.setStart(valueStart);
								range.setEnd(valueEnd);

								Location location = new Location(path.toUri().toASCIIString(), new Range(valueStart, valueEnd));
								
								Position keyStart = new Position(nodeTuple.getKeyNode().getStartMark().getLine(), nodeTuple.getKeyNode().getStartMark().getColumn());
								
								targetRanges.put(location, new Range(keyStart, valueEnd));
								return Optional.of(location);
							}));
						}
					}
				}
			}
		});
		return links.build();
	}

	private static String extractPropertyKey(String s) {
		if (s.length() > 3 && (s.startsWith("${") || s.startsWith("#{")) && s.endsWith("}")) {
			return s.substring(2, s.length() - 1);
		}
		return null;
	}

}
