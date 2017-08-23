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
package org.springframework.ide.vscode.boot.java.hover;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.json.JSONObject;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class ValueHoverProvider {

	public CompletableFuture<Hover> provideHoverForValueAnnotation(ASTNode node, Annotation annotation,
			ITypeBinding type, int offset, TextDocument doc) {

		try {
			// case: @Value("prefix<*>")
			if (node instanceof StringLiteral && node.getParent() instanceof Annotation) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideHover(node.toString(), offset - node.getStartPosition(), node.getStartPosition(), doc);
				}
			}
			// case: @Value(value="prefix<*>")
			else if (node instanceof StringLiteral && node.getParent() instanceof MemberValuePair
					&& "value".equals(((MemberValuePair)node.getParent()).getName().toString())) {
				if (node.toString().startsWith("\"") && node.toString().endsWith("\"")) {
					return provideHover(node.toString(), offset - node.getStartPosition(), node.getStartPosition(), doc);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private CompletableFuture<Hover> provideHover(String value, int offset, int nodeStartOffset, TextDocument doc) {

		try {
			LocalRange range = getPropertyRange(value, offset);
			if (range != null) {
				String propertyKey = value.substring(range.getStart(), range.getEnd());

				if (propertyKey != null) {
					JSONObject[] allProperties = getPropertiesFromProcesses();

					List<Either<String, MarkedString>> hoverContent = new ArrayList<>();

					for (int i = 0; i < allProperties.length; i++) {
						Iterator<?> keys = allProperties[i].keys();
						while (keys.hasNext()) {
							String key = (String) keys.next();
							if (allProperties[i].get(key) instanceof JSONObject) {
								JSONObject props = allProperties[i].getJSONObject(key);

								if (props.has(propertyKey)) {
									String propertyValue = props.getString(propertyKey);

									hoverContent.add(Either.forLeft("property value for " + propertyKey));
									hoverContent.add(Either.forLeft(propertyValue));
									hoverContent.add(Either.forLeft("coming from:"));
									hoverContent.add(Either.forLeft(key));
								}
							}
						}
					}

					Range hoverRange = doc.toRange(nodeStartOffset + range.getStart(), range.getEnd() - range.getStart());
					Hover hover = new Hover();

					hover.setContents(hoverContent);
					hover.setRange(hoverRange);

					return CompletableFuture.completedFuture(hover);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public JSONObject[] getPropertiesFromProcesses() {
		List<JSONObject> result = new ArrayList<>();

		try {
			Map<String, SpringBootApp> apps = SpringBootApp.getAllRunningJavaApps();
			Iterator<SpringBootApp> appsIter = apps.values().iterator();
			while (appsIter.hasNext()) {
				SpringBootApp app = appsIter.next();
				if (app.isSpringBootApp()) {
					String environment = app.getEnvironment();
					if (environment != null) {
						JSONObject env = new JSONObject(environment);
						if (env != null) {
							result.add(env);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return result.toArray(new JSONObject[result.size()]);
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

}
