/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.LiveHoverUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.liveproperties.LiveProperties;
import org.springframework.ide.vscode.commons.boot.app.cli.liveproperties.LiveProperty;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class ValueHoverProvider implements HoverProvider {

	protected static Logger logger = LoggerFactory.getLogger(ValueHoverProvider.class);

	@Override
	public Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc,
			IJavaProject project, SpringBootApp[] runningApps) {

		try {
			ASTNode foundNode = NodeFinder.perform(node, offset, 0);
			ASTNode exactNode = getExactNode(foundNode);

			if (exactNode != null) {
				return provideHover(exactNode.toString(), offset - exactNode.getStartPosition(),
						exactNode.getStartPosition(), doc, runningApps);
			}
		} catch (Exception e) {
			logger.error("Error while generating live hovers for @Value", e);
		}

		return null;
	}

	private ASTNode getExactNode(ASTNode exactNode) {
		if (exactNode != null) {
			// case: @Value("prefix<*>")
			if (exactNode instanceof StringLiteral && exactNode.getParent() instanceof Annotation) {
				if (exactNode.toString().startsWith("\"") && exactNode.toString().endsWith("\"")) {
					return exactNode;
				}
			}
			// case: @Value(value="prefix<*>")
			else if (exactNode instanceof StringLiteral && exactNode.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair) exactNode.getParent()).getName().toString())) {
				if (exactNode.toString().startsWith("\"") && exactNode.toString().endsWith("\"")) {
					return exactNode;
				}
			}
		}
		return null;
	}

	private Hover provideHover(String value, int offset, int nodeStartOffset, TextDocument doc, SpringBootApp[] runningApps) {

		try {
			LocalRange range = parsePropertyOnHoverOffset(value, offset);
			if (range != null) {
				String propertyKey = value.substring(range.getStart(), range.getEnd());

				if (propertyKey != null) {
					Map<SpringBootApp, LiveProperties> allProperties = getPropertiesFromProcesses(runningApps);

					StringBuilder hover = new StringBuilder();

					for (SpringBootApp app : allProperties.keySet()) {
						LiveProperties properties = allProperties.get(app);
						List<LiveProperty> foundProperties = properties.getProperties(propertyKey);
						if (foundProperties != null) {
							for (LiveProperty liveProp : foundProperties) {
								hover.append(propertyKey + " : " + liveProp.getValue());
								hover.append(" (from: " + liveProp.getSource() + ")\n\n");
								hover.append(LiveHoverUtils.niceAppName(app));
								hover.append("\n\n");
							}
						}
					}

					if (hover.length() > 0) {
						Range hoverRange = doc.toRange(nodeStartOffset + range.getStart(), range.getEnd() - range.getStart());
						Hover result = new Hover(ImmutableList.of(Either.forLeft(hover.toString())));
						result.setRange(hoverRange);

						return result;
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Error while generating live hovers for @Value", e);
		}

		return null;
	}

	/**
	 * Highlight hints for live hover information is provided separately from the
	 * actual live hovers. To provide highlight hints, we only need to find if, for
	 * the given String literal node, it contains a property that has live
	 * properties, but we don't need to compute all the live properties for the
	 * hint. We only need to compute a code lens with the range of the property that
	 * has matching live properties.
	 *
	 * @param doc
	 * @param node
	 * @param runningApps
	 * @return
	 */
	private List<CodeLens> provideHighlightHints(TextDocument doc, StringLiteral node, SpringBootApp[] runningApps) {
		ASTNode exactNode = getExactNode(node);

		if (exactNode != null) {
			try {
				String propFromValue = null;
				LocalRange propRange = null;

				// Get the escaped value that INCLUDES the quotes as we want to compute
				// the hint range in the editor of the property and need to take into account
				// all characters in the node value.
				// For example: @Value(value = "${a.prop}")
				// to highlight a.prop we need to take into account the starting '"' after the '='
				// to get the correct range of a.prop
				String nodeValue = node.getEscapedValue();
				if (nodeValue != null) {
					// Get actual range and property from the node, as to highlight it
					propRange = parseProperty(nodeValue);
					if (propRange != null) {
						propFromValue = nodeValue.substring(propRange.getStart(), propRange.getEnd());
					}
				}

				// Now find live information for the property. If found, highlight that property via a Code Lens
				if (propFromValue != null && propRange != null) {
					List<LiveProperty> matchingLiveProperties = findMatchingLiveProperties(runningApps, propFromValue);

					if (matchingLiveProperties != null && !matchingLiveProperties.isEmpty()) {
						Range hoverRange = doc.toRange(exactNode.getStartPosition() + propRange.getStart(),
								propRange.getEnd() - propRange.getStart());
						return ImmutableList.of(new CodeLens(hoverRange));
					}
				}
			} catch (Exception e) {
				logger.error("Error while generating highlight hints for properties in @Value", e);
			}
		}
		return ImmutableList.of();
	}

	private List<LiveProperty> findMatchingLiveProperties(SpringBootApp[] runningApps, String propFromValue) {
		Map<SpringBootApp, LiveProperties> allProperties = getPropertiesFromProcesses(runningApps);

		for (SpringBootApp app : allProperties.keySet()) {
			LiveProperties properties = allProperties.get(app);
			List<LiveProperty> matchingLiveProperties = properties.getProperties(propFromValue);
			if (matchingLiveProperties != null && !matchingLiveProperties.isEmpty()) {
				return matchingLiveProperties;
			}
		}
		return null;
	}

	public Map<SpringBootApp, LiveProperties> getPropertiesFromProcesses(SpringBootApp[] runningApps) {
		Map<SpringBootApp, LiveProperties> result = new HashMap<>();

		try {
			for (SpringBootApp app : runningApps) {
				LiveProperties liveProperties = app.getLiveProperties();
				if (liveProperties != null) {
					result.put(app, liveProperties);
				}
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}

		return result;
	}

	public String getPropertyKey(String value, int offset) {
		LocalRange range = parsePropertyOnHoverOffset(value, offset);
		if (range != null) {
			return value.substring(range.getStart(), range.getEnd());
		}
		return null;
	}

	/**
	 * Find the range of a property in the given value, starting from an offset point, where the offset is
	 * the hover cursor location. This will only attempt to parse the property from the value, IFF the offset is on the
	 * property itself, but not on '$', '{', or '}'. For example, for "${a.prop}", property "a.prop" will only be parsed
	 * if offset is anywhere within the range of the "a.prop", but not '$', '{', or '}'.
	 * @param value
	 * @param offset
	 * @return
	 */
	private LocalRange parsePropertyOnHoverOffset(String value, int offset) {
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

	private LocalRange parseProperty(String value) {
		int start = -1;
		int end = -1;

		for (int i = value.length() - 1; i >= 0; i--) {
			if (value.charAt(i) == '{') {
				start = i + 1;
				break;
			}
		}

		for(int i = 0; i < value.length(); i++) {

			if (value.charAt(i) == '}') {
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

	@Override
	public Hover provideHover(ASTNode node, TypeDeclaration typeDeclaration, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return null;
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		// Show highlight hints for properties in @Value that have live information

		List<CodeLens> lenses = new ArrayList<>();
		annotation.accept(new ASTVisitor() {
			@Override
			public boolean visit(StringLiteral node) {
				List<CodeLens> provideHighlightHints = provideHighlightHints(doc, node, runningApps);
				lenses.addAll(provideHighlightHints);
				return super.visit(node);
			}
		});

		return lenses;
	}

}
