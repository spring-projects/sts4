/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveProperties;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveProperty;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class ValueHoverProvider implements HoverProvider {

	protected static Logger logger = LoggerFactory.getLogger(ValueHoverProvider.class);
	// Match pattern: matches ${} OR {}. For '$' cases, it also handles whitespaces
	// between the '$' and '{' as this is still valid place holder in Spring Boot, so
	// example: $   {a.prop}. It won't match something like this ${{} or ${$} , but will match
	// nested cases: ${${a.prop}}
	private static Pattern PROPERTY_PLACEHOLDER = Pattern.compile("\\$?\\{([^\\}^\\{^\\$]+)\\}");


	@Override
	public Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc,
			IJavaProject project, SpringProcessLiveData[] processLiveData) {

		try {
			ASTNode foundNode = NodeFinder.perform(node, offset, 0);
			ASTNode exactNode = getExactNode(foundNode);

			if (exactNode != null) {
				return provideHover(exactNode.toString(), offset - exactNode.getStartPosition(),
						exactNode.getStartPosition(), doc, processLiveData);
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

	private Hover provideHover(String value, int offset, int nodeStartOffset, TextDocument doc, SpringProcessLiveData[] processLiveData) {

		try {
			LocalRange range = parsePropertyOnHoverOffset(value, offset);
			if (range != null) {
				String propertyKey = value.substring(range.getStart(), range.getEnd());

				if (propertyKey != null) {
					Map<SpringProcessLiveData, LiveProperties> allProperties = getPropertiesFromProcesses(processLiveData);

					StringBuilder hover = new StringBuilder();

					for (SpringProcessLiveData app : allProperties.keySet()) {
						LiveProperties properties = allProperties.get(app);
						List<LiveProperty> foundProperties = properties.getProperties(propertyKey);
						if (foundProperties != null) {
							for (LiveProperty liveProp : foundProperties) {
								Renderable renderable = Renderables.concat(Renderables.mdBlob(propertyKey + " : " + liveProp.getValue() + " (from: " + liveProp.getSource() + ")"),
										Renderables.lineBreak(),
										Renderables.mdBlob(LiveHoverUtils.niceAppName(app)),
										Renderables.text("\n\n")		
								);
								hover.append(renderable.toMarkdown());				
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
	 * @param processLiveData
	 * @return
	 */
	private List<CodeLens> provideHighlightHints(TextDocument doc, StringLiteral node, SpringProcessLiveData[] processLiveData) {
		ASTNode exactNode = getExactNode(node);

		if (exactNode != null) {
			Map<String, List<LocalRange>> propertiesWithRanges = new HashMap<>();

			// Get the escaped value that INCLUDES the quotes as we want to compute
			// the hint range in the editor of the property and need to take into account
			// all characters in the node value.
			// For example: @Value(value = "${a.prop}")
			// to highlight a.prop we need to take into account the starting '"' after the
			// '='
			// to get the correct range of a.prop
			String nodeValue = node.getEscapedValue();
			if (nodeValue != null) {
				// Get property names with ranges highlight
				propertiesWithRanges = parseProperties(nodeValue);
			}

			// Now find live information for the properties. If found, highlight that property
			// via a Code Lens
			if (propertiesWithRanges != null && !propertiesWithRanges.isEmpty()) {

				List<CodeLens> lenses = new ArrayList<>();
				// Just find one of the errors to log
				final Exception[] error = new Exception[1];
				propertiesWithRanges.entrySet().stream().forEach(entry -> {
					String parsedProp = entry.getKey();
					List<LocalRange> propRanges = entry.getValue();

					List<LiveProperty> matchingLiveProperties = findMatchingLiveProperties(processLiveData, parsedProp);
					if (matchingLiveProperties != null && !matchingLiveProperties.isEmpty()) {

						propRanges.stream().forEach(propRange -> {
							try {
								Range hoverRange = doc.toRange(exactNode.getStartPosition() + propRange.getStart(),
										propRange.getEnd() - propRange.getStart());
								lenses.add(new CodeLens(hoverRange));
							} catch (BadLocationException e) {
								error[0] = e;
							}
						});
					}
				});
				if (error[0] != null) {
					logger.error("Error while generating hints for properties in @Value", error[0]);
				}
				return lenses;
			}
		}
		return ImmutableList.of();
	}

	private List<LiveProperty> findMatchingLiveProperties(SpringProcessLiveData[] processLiveData, String propFromValue) {
		Map<SpringProcessLiveData, LiveProperties> allProperties = getPropertiesFromProcesses(processLiveData);

		for (SpringProcessLiveData liveData : allProperties.keySet()) {
			LiveProperties properties = allProperties.get(liveData);
			List<LiveProperty> matchingLiveProperties = properties.getProperties(propFromValue);
			if (matchingLiveProperties != null && !matchingLiveProperties.isEmpty()) {
				return matchingLiveProperties;
			}
		}
		return null;
	}

	public Map<SpringProcessLiveData, LiveProperties> getPropertiesFromProcesses(SpringProcessLiveData[] processLiveData) {
		Map<SpringProcessLiveData, LiveProperties> result = new HashMap<>();

		try {
			for (SpringProcessLiveData liveData : processLiveData) {
				LiveProperties liveProperties = liveData.getLiveProperties();
				if (liveProperties != null) {
					result.put(liveData, liveProperties);
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

	private Map<String, List<LocalRange>> parseProperties(String literalExpression) {
		Matcher matcher = PROPERTY_PLACEHOLDER.matcher(literalExpression);

		Map<String, List<LocalRange>> propsWithRanges = new HashMap<>();
		// Matches each ${} placeholder encountered. Extract property name for each one.
		// Beware that same property name may appear more than once in the expression,
		// and
		// therefore will have multiple ranges. Example: @Value("${a.prop} ${b.prop}
		// ${a.prop}")

		while (matcher.find()) {

			// Found the next ${} placeholder. Now find the range IN the literal expression
			// of the property name WITHIN that placeholder
			// (so excluding the '$', '{', '}' chars)
			LocalRange propRange = findPropRangeInPlaceholder(literalExpression, matcher.start(), matcher.end());
			if (propRange != null) {
				String propName = literalExpression.substring(propRange.getStart(), propRange.getEnd());
				List<LocalRange> ranges = propsWithRanges.get(propName);
				if (ranges == null) {
					ranges = new ArrayList<>();
					propsWithRanges.put(propName, ranges);
				}
				ranges.add(propRange);
			}
		}

		return propsWithRanges;
	}

	private LocalRange findPropRangeInPlaceholder(String literalExpression, int placeHolderStart,
			int placeHolderOffset) {
		int start = -1;
		int end = -1;

		if (placeHolderStart < 0 || placeHolderStart >= literalExpression.length()
				|| placeHolderOffset <= placeHolderStart || placeHolderOffset > literalExpression.length()) {
			return null;
		}

		for (int i = placeHolderStart; i < placeHolderOffset; i++) {
			if (literalExpression.charAt(i) == '{') {
				start = i + 1;
			} else if (literalExpression.charAt(i) == '}') {
				end = i;
			}
		}
		if (start > 0 && start < literalExpression.length() && end > 0 && end <= literalExpression.length()
				&& start < end) {
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
			TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {
		return null;
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc, SpringProcessLiveData[] processLiveData) {
		// Show highlight hints for properties in @Value that have live information

		List<CodeLens> lenses = new ArrayList<>();
		annotation.accept(new ASTVisitor() {
			@Override
			public boolean visit(StringLiteral node) {
				List<CodeLens> provideHighlightHints = provideHighlightHints(doc, node, processLiveData);
				lenses.addAll(provideHighlightHints);
				return super.visit(node);
			}
		});

		return lenses;
	}

}
