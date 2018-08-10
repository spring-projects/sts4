/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import org.springframework.ide.vscode.boot.java.livehover.LiveHoverUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;
import org.springframework.ide.vscode.commons.java.IJavaProject;
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
	public Hover provideHover(ASTNode node, Annotation annotation,
			ITypeBinding type, int offset, TextDocument doc, IJavaProject project, SpringBootApp[] runningApps) {
		return provideHover(annotation, doc, runningApps);
	}

	@Override
	public Collection<Range> getLiveHoverHints(IJavaProject project, Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
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

	private Hover provideHover(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {

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

			return hover;
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

		if (rqClassName != null) {
			int chop = rqClassName.indexOf("$$EnhancerBySpringCGLIB$$");
			if (chop >= 0) {
				rqClassName = rqClassName.substring(0, chop);
			}

			rqClassName = rqClassName.replace('$', '.');

			ASTNode parent = annotation.getParent();
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration methodDec = (MethodDeclaration) parent;
				IMethodBinding binding = methodDec.resolveBinding();
				if (binding != null) {
					return binding.getDeclaringClass().getQualifiedName().equals(rqClassName)
							&& binding.getName().equals(rm.getMethodName())
							&& Arrays.equals(Arrays.stream(binding.getParameterTypes())
									.map(t -> t.getTypeDeclaration().getQualifiedName())
									.toArray(String[]::new),
								rm.getMethodParameters());
				}
	//		} else if (parent instanceof TypeDeclaration) {
	//			TypeDeclaration typeDec = (TypeDeclaration) parent;
	//			return typeDec.resolveBinding().getQualifiedName().equals(rqClassName);
			}
		}
		return false;
	}

	private void addHoverContent(List<Tuple2<RequestMapping, SpringBootApp>> mappingMethods, List<Either<String, MarkedString>> hoverContent) throws Exception {
		for (int i = 0; i < mappingMethods.size(); i++) {
			Tuple2<RequestMapping, SpringBootApp> mappingMethod = mappingMethods.get(i);

			SpringBootApp app = mappingMethod.getT2();
			String port = mappingMethod.getT2().getPort();
			String host = mappingMethod.getT2().getHost();

			String[] paths = mappingMethod.getT1().getSplitPath();
			if (paths==null || paths.length==0) {
				//Technically, this means the path 'predicate' is unconstrained, meaning any path matches.
				//So this is not quite the same as the case where path=""... but...
				//It is better for us to show one link where any path is allowed, versus showing no links where any link is allowed.
				//So we'll pretend this is the same as path="" as that gives a working link.
				paths = new String[] {""};
			}
			List<Renderable> renderableUrls = Arrays.stream(paths).flatMap(path -> {
				String url = UrlUtil.createUrl(host, port, path);
				return Stream.of(Renderables.link(url, url), Renderables.lineBreak());
			})
			.collect(Collectors.toList());

			//The renderableUrls can not be empty because, above, we made sure there's at least one path.
			renderableUrls.remove(renderableUrls.size() - 1);

			hoverContent.add(Either.forLeft(Renderables.concat(renderableUrls).toMarkdown()));
			hoverContent.add(Either.forLeft(LiveHoverUtils.niceAppName(app)));
			if (i < mappingMethods.size() - 1) {
				// Three dashes == line separator in Markdown
				hoverContent.add(Either.forLeft("---"));
			}

		}
	}

}
