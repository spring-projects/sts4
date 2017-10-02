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
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.json.JSONObject;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class RequestMappingHoverProvider implements HoverProvider {

	@Override
	public CompletableFuture<Hover> provideHover(ASTNode node, Annotation annotation,
			ITypeBinding type, int offset, TextDocument doc, SpringBootApp[] runningApps) {
		return provideHover(annotation, doc, runningApps);
	}

	@Override
	public Range getLiveHoverHint(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		try {
			if (runningApps.length > 0) {
				// TODO: this check is too simple, we need to do a lot more here
				// -> check if the running app has a matching request mapping for this annotation

				Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
				return hoverRange;
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}

		return null;
	}

	private CompletableFuture<Hover> provideHover(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {

		try {
			JSONObject[] mappings = getRequestMappingsFromProcesses(runningApps);

			List<Either<String, MarkedString>> hoverContent = new ArrayList<>();

			for (int i = 0; i < mappings.length; i++) {
				addHoverContent(mappings[i], hoverContent, annotation);
			}

			Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
			Hover hover = new Hover();

			hover.setContents(hoverContent);
			hover.setRange(hoverRange);

			return CompletableFuture.completedFuture(hover);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void addHoverContent(JSONObject jsonObject, List<Either<String, MarkedString>> hoverContent, Annotation annotation) {
		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (doesMatch(annotation, key)) {
				hoverContent.add(Either.forLeft(key + " - [url of the mapping](http://localhost:8080/path)"));
				hoverContent.add(Either.forLeft(jsonObject.get(key).toString()));
			}
		}
	}

	private boolean doesMatch(Annotation annotation, String key) {
		String mappingPath = null;
		if (annotation instanceof SingleMemberAnnotation) {
			Expression valueContent = ((SingleMemberAnnotation) annotation).getValue();
			if (valueContent instanceof StringLiteral) {
				mappingPath = ((StringLiteral)valueContent).getLiteralValue();
			}
		}
		else if (annotation instanceof NormalAnnotation) {
			List<?> values = ((NormalAnnotation) annotation).values();
			for (Object value : values) {
				if (value instanceof MemberValuePair) {
					String name = ((MemberValuePair)value).getName().toString();
					if (name != null && name.equals("value")) {
						Expression valueContent = ((MemberValuePair)value).getValue();
						if (valueContent instanceof StringLiteral) {
							mappingPath = ((StringLiteral)valueContent).getLiteralValue();
						}
					}
				}
			}

		}

		return mappingPath != null ? key.contains(mappingPath) : false;
	}

	public JSONObject[] getRequestMappingsFromProcesses(SpringBootApp[] runningApps) {
		List<JSONObject> result = new ArrayList<>();

		try {
			for (SpringBootApp app : runningApps) {
				String mappings = app.getRequestMappings();
				if (mappings != null) {
					JSONObject requestMappings = new JSONObject(mappings);
					if (requestMappings != null) {
						result.add(requestMappings);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return result.toArray(new JSONObject[result.size()]);
	}

}
