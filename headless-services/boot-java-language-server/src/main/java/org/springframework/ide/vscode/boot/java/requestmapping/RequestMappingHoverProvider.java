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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
	public Collection<Range> getLiveHoverHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		try {
			if (runningApps.length > 0) {
				List<Tuple2<RequestMapping, SpringBootApp>> val = getRequestMappingMethodFromRunningApp(annotation, runningApps);
				if (!val.isEmpty()) {
					Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
					return ImmutableList.of(hoverRange);
				}
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

			List<Tuple2<RequestMapping, SpringBootApp>> val = getRequestMappingMethodFromRunningApp(annotation, runningApps);

			if (!val.isEmpty()) {
				addHoverContent(val, hoverContent);
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

	private List<Tuple2<RequestMapping, SpringBootApp>> getRequestMappingMethodFromRunningApp(Annotation annotation,
			SpringBootApp[] runningApps) {

		List<Tuple2<RequestMapping, SpringBootApp>> results = new ArrayList<>();
		try {
			for (SpringBootApp app : runningApps) {
				Collection<RequestMapping> mappings = app.getRequestMappings();
				if (mappings != null && !mappings.isEmpty()) {
					mappings.stream()
//							.filter(rm -> matchesAnnotation(annotation, rm))
							.filter(rm -> methodMatchesAnnotation(annotation, rm))
							.map(rm -> Tuples.of(rm, app))
							.findFirst().ifPresent(t -> results.add(t));
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return results;
	}

	private boolean methodMatchesAnnotation(Annotation annotation, RequestMapping rm) {
		String rqClassName = rm.getFullyQualifiedClassName();

		ASTNode parent = annotation.getParent();
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration methodDec = (MethodDeclaration) parent;
			IMethodBinding binding = methodDec.resolveBinding();
			return binding.getDeclaringClass().getQualifiedName().equals(rqClassName)
					&& binding.getName().equals(rm.getMethodName())
					&& Arrays.equals(Arrays.stream(binding.getParameterTypes())
							.map(t -> t.getTypeDeclaration().getQualifiedName())
							.toArray(String[]::new),
						rm.getMethodParameters());
//		} else if (parent instanceof TypeDeclaration) {
//			TypeDeclaration typeDec = (TypeDeclaration) parent;
//			return typeDec.resolveBinding().getQualifiedName().equals(rqClassName);
		}
		return false;
	}

	private void addHoverContent(List<Tuple2<RequestMapping, SpringBootApp>> mappingMethods, List<Either<String, MarkedString>> hoverContent) throws Exception {
		for (int i = 0; i < mappingMethods.size(); i++) {
			Tuple2<RequestMapping, SpringBootApp> mappingMethod = mappingMethods.get(i);

			String processId = mappingMethod.getT2().getProcessID();
			String processName = mappingMethod.getT2().getProcessName();
			String port = mappingMethod.getT2().getPort();
			String host = mappingMethod.getT2().getHost();

			 List<Renderable> renderableUrls = Arrays.stream(mappingMethod.getT1().getSplitPath()).flatMap(path -> {
				String url = UrlUtil.createUrl(host, port, path);
				StringBuilder builder = new StringBuilder();
				builder.append("[");
				builder.append(url);
				builder.append("]");
				builder.append("(");
				builder.append(url);
				builder.append(")");
				return Stream.of(Renderables.text(builder.toString()), Renderables.lineBreak());
			})
			.collect(Collectors.toList());

			 // Remove the last line break
			 renderableUrls.remove(renderableUrls.size() - 1);

			hoverContent.add(Either.forLeft(Renderables.concat(renderableUrls).toMarkdown()));
			hoverContent.add(Either.forLeft("Process ID: " + processId));
			hoverContent.add(Either.forLeft("Process Name: " + processName));
			if (i < mappingMethods.size() - 1) {
				// Three dashes == line separator in Markdown
				hoverContent.add(Either.forLeft("---"));
			}

		}
	}

//	protected String getRequestMethod(SingleMemberAnnotation annotation) {
//		ITypeBinding type = annotation.resolveTypeBinding();
//		if (type != null) {
//			switch (type.getQualifiedName()) {
//			case Constants.SPRING_GET_MAPPING:
//				return "GET";
//			case Constants.SPRING_POST_MAPPING:
//				return "POST";
//			case Constants.SPRING_DELETE_MAPPING:
//				return "DELETE";
//			case Constants.SPRING_PUT_MAPPING:
//				return "PUT";
//			case Constants.SPRING_PATCH_MAPPING:
//				return "PATCH";
//			}
//		}
//		return null;
//	}
//
//	private boolean matchesAnnotation(Annotation annotation, RequestMapping rm) {
//		String[] mappingPath = null;
//		Set<String> methods = null;
//		if (annotation instanceof SingleMemberAnnotation) {
//			SingleMemberAnnotation singleAnnotation = (SingleMemberAnnotation) annotation;
//			Expression valueContent = singleAnnotation.getValue();
//			if (valueContent instanceof StringLiteral) {
//				mappingPath = new String[] { ((StringLiteral)valueContent).getLiteralValue() };
//			}
//			String method = getRequestMethod(singleAnnotation);
//			if (method != null) {
//				methods = new HashSet<>();
//				methods.add(method);
//			}
//		}
//		else if (annotation instanceof NormalAnnotation) {
//			List<?> values = ((NormalAnnotation) annotation).values();
//			for (Object value : values) {
//				if (value instanceof MemberValuePair) {
//					MemberValuePair pair = (MemberValuePair)value;
//					String name = pair.getName().toString();
//					switch (name) {
//					case "value":
//					case "path":
//						mappingPath = getPaths(pair.getValue());
//						break;
//					case "method":
//						methods = getRequestMethod(pair.getValue());
//						break;
//					}
//				}
//			}
//
//		}
//
//		if (mappingPath != null) {
//			if (Arrays.equals(mappingPath, rm.getSplitPath())) {
//				if (methods == null || methods.isEmpty()) {
//					return true;
//				} else {
//					return methods.equals(rm.getRequestMethods());
//				}
//			}
//		}
//		return false;
//	}
//
//	private static String getExpressionValueAsString(Expression exp) {
//		if (exp instanceof StringLiteral) {
//			return ((StringLiteral)exp).getLiteralValue();
//		} else if (exp instanceof QualifiedName) {
//			return getExpressionValueAsString(((QualifiedName)exp).getName());
//		} else if (exp instanceof SimpleName) {
//			return ((SimpleName)exp).getIdentifier();
//		} else {
//			return null;
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	private static String[] getPaths(Expression exp) {
//		if (exp instanceof ArrayInitializer) {
//			ArrayInitializer array = (ArrayInitializer) exp;
//			return ((List<Expression>)array.expressions()).stream()
//					.map(e -> getExpressionValueAsString(e))
//					.filter(Objects::nonNull)
//					.toArray(String[]::new);
//		} else {
//			String rm = getExpressionValueAsString(exp);
//			if (rm != null) {
//				return new String[] { rm };
//			}
//		}
//		return null;
//	}
//
//	@SuppressWarnings("unchecked")
//	private static Set<String> getRequestMethod(Expression exp) {
//		if (exp instanceof ArrayInitializer) {
//			ArrayInitializer array = (ArrayInitializer) exp;
//			return ((List<Expression>)array.expressions()).stream()
//					.map(e -> getExpressionValueAsString(e))
//					.filter(Objects::nonNull)
//					.collect(Collectors.toSet());
//		} else {
//			String rm = getExpressionValueAsString(exp);
//			if (rm != null) {
//				HashSet<String> methods = new HashSet<>();
//				methods.add(rm);
//			}
//		}
//		return null;
//	}

}
