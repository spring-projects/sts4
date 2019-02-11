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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
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

	@Override
	public Hover provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {

		try {
			ASTNode exactNode = NodeFinder.perform(node, offset, 0);

			// case: @Value("prefix<*>")
			if (exactNode != null && exactNode instanceof StringLiteral && exactNode.getParent() instanceof Annotation) {
				if (exactNode.toString().startsWith("\"") && exactNode.toString().endsWith("\"")) {
					return provideHover(exactNode.toString(), offset - exactNode.getStartPosition(), exactNode.getStartPosition(), doc, runningApps);
				}
			}
			// case: @Value(value="prefix<*>")
			else if (exactNode != null && exactNode instanceof StringLiteral && exactNode.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)exactNode.getParent()).getName().toString())) {
				if (exactNode.toString().startsWith("\"") && exactNode.toString().endsWith("\"")) {
					return provideHover(exactNode.toString(), offset - exactNode.getStartPosition(), exactNode.getStartPosition(), doc, runningApps);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private Hover provideHover(String value, int offset, int nodeStartOffset, TextDocument doc, SpringBootApp[] runningApps) {

		try {
			LocalRange range = getPropertyRange(value, offset);
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
			e.printStackTrace();
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
			e.printStackTrace();
		}

		return result;
	}

	public String getPropertyKey(String value, int offset) {
		LocalRange range = getPropertyRange(value, offset);
		if (range != null) {
			return value.substring(range.getStart(), range.getEnd());
		}
		return null;
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

	@Override
	public Hover provideHover(ASTNode node, TypeDeclaration typeDeclaration, ITypeBinding type, int offset,
			TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return null;
	}

}
