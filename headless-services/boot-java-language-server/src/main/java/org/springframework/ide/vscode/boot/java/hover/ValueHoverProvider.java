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

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
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
import org.json.JSONTokener;
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

				JSONObject properties = getPropertiesFromProcess();
				if (propertyKey != null && properties != null) {
					Iterator<?> keys = properties.keys();
					while (keys.hasNext()) {
						String key = (String) keys.next();
						JSONObject props = properties.getJSONObject(key);

						if (props.has(propertyKey)) {
							String propertyValue = props.getString(propertyKey);

							Range hoverRange = doc.toRange(nodeStartOffset + range.getStart(), range.getEnd() - range.getStart());

							Hover hover = new Hover();
							List<Either<String, MarkedString>> hoverContent = new ArrayList<>();

							hoverContent.add(Either.forLeft("property value for " + propertyKey));
							hoverContent.add(Either.forLeft(propertyValue));
							hoverContent.add(Either.forLeft("coming from:"));
							hoverContent.add(Either.forLeft(key));

							hover.setContents(hoverContent);
							hover.setRange(hoverRange);

							return CompletableFuture.completedFuture(hover);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public JSONObject getPropertiesFromProcess() {
		try {
			URL url = new URL("http://localhost:8080/env");

			URLConnection con = url.openConnection();
			InputStream in = con.getInputStream();
			String encoding = con.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;
			String body = IOUtils.toString(in, encoding);

			JSONTokener tokener = new JSONTokener(body);
			JSONObject jsonData = new JSONObject(tokener);

			return jsonData;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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
