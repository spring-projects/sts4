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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.json.JSONObject;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.parser.JLRMethodParser;
import org.springframework.ide.vscode.commons.java.parser.JLRMethodParser.JLRMethod;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
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
			Log.log(e);
		}

		return null;
	}

	private CompletableFuture<Hover> provideHover(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {

		try {
			List<Either<String, MarkedString>> hoverContent = new ArrayList<>();

			Optional<RequestMappingMethod> val = getRequestMappingMethodFromRunningApp(annotation, runningApps);

			if (val.isPresent()) {
				addHoverContent(val.get(), hoverContent);
			}

			Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
			Hover hover = new Hover();

			hover.setContents(hoverContent);
			hover.setRange(hoverRange);

			return CompletableFuture.completedFuture(hover);
		} catch (Exception e) {
			Log.log(e);
		}

		return null;
	}

	private Optional<RequestMappingMethod> getRequestMappingMethodFromRunningApp(Annotation annotation,
			SpringBootApp[] runningApps) {

		try {
			for (SpringBootApp app : runningApps) {
				String mappings = app.getRequestMappings();
				JSONObject requestMappings = new JSONObject(mappings);

				String rawPath = getRawPath(annotation, requestMappings);
				if (rawPath != null) {
					String path = UrlUtil.extractPath(rawPath);
					if (path != null) {
						String rawMethod = getRawMethod(annotation, requestMappings);
						JLRMethod parsedMethod = JLRMethodParser.parse(rawMethod);
						if (methodMatchesAnnotation(annotation, parsedMethod)) {
							return Optional.of(new RequestMappingMethod(path, parsedMethod, app));
						}
					}
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return Optional.empty();
	}

	private boolean methodMatchesAnnotation(Annotation annotation, JLRMethod requestMappingMethod) {
		String rqClassName = requestMappingMethod.getFQClassName();
		String rqMethod = requestMappingMethod.getMethodName();

		ASTNode parent = annotation.getParent();
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration methodDec = (MethodDeclaration) parent;
			IMethodBinding binding = methodDec.resolveBinding();
			return binding.getDeclaringClass().getQualifiedName().equals(rqClassName) &&
					binding.getName().equals(rqMethod);
		} else if (parent instanceof TypeDeclaration) {
			TypeDeclaration typeDec = (TypeDeclaration) parent;
			return typeDec.resolveBinding().getQualifiedName().equals(rqClassName);
		}
		return false;
	}

	private void addHoverContent(RequestMappingMethod mappingMethod, List<Either<String, MarkedString>> hoverContent) throws Exception {
		String processId = mappingMethod.app.getProcessID();
		String processName = mappingMethod.app.getProcessName();
		String path = mappingMethod.requestMappingPath;

		StringBuilder builder = new StringBuilder();

		String port = mappingMethod.app.getPort();
		String host = mappingMethod.app.getHost();
		String url = UrlUtil.createUrl(host, port, path);

		builder.append("Path: ");
		builder.append("[");
		builder.append(path);
		builder.append("]");
		builder.append("(");
		builder.append(url);
		builder.append(")");

		hoverContent.add(Either.forLeft(builder.toString()));
		hoverContent.add(Either.forLeft("Process ID: " + processId));
		hoverContent.add(Either.forLeft("Process Name: " + processName));
	}

	private String getRawMethod(Annotation annotation, JSONObject mappings) {
		Iterator<String> keys = mappings.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (matchesAnnotation(annotation, key)) {
				Object ob= mappings.get(key);
				if (ob instanceof JSONObject) {
					JSONObject methodMap = (JSONObject) ob;
					return methodMap.getString("method");
				}
			}
		}
		return null;
	}

	private String getRawPath(Annotation annotation, JSONObject mappings) {
		Iterator<String> keys = mappings.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (matchesAnnotation(annotation, key)) {
				return key;
			}
		}
		return null;
	}

	private boolean matchesAnnotation(Annotation annotation, String jsonKey) {
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

		return jsonKey.contains(mappingPath);
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
			Log.log(e);
		}

		return result.toArray(new JSONObject[result.size()]);
	}

	static class RequestMappingMethod {

		public final SpringBootApp app;
		public final String requestMappingPath;
		public final JLRMethod requestMappingMethod;

		public RequestMappingMethod(String requestMappingPath, JLRMethod requestMappingMethod, SpringBootApp app) {
			this.requestMappingPath = requestMappingPath;
			this.requestMappingMethod = requestMappingMethod;
			this.app = app;
		}
	}
}
