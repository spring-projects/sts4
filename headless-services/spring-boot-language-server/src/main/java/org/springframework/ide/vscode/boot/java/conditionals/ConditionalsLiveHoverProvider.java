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
package org.springframework.ide.vscode.boot.java.conditionals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.livehover.LiveHoverUtils;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveConditional;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 *
 * Provides live hovers and hints for @ConditionalOn... Spring Boot annotations
 * from running spring boot apps.
 */
public class ConditionalsLiveHoverProvider implements HoverProvider {

	private static final Logger log = LoggerFactory.getLogger(ConditionalsLiveHoverProvider.class);

	@Override
	public Hover provideHover(J node, Annotation annotation, int offset,
			TextDocument doc, IJavaProject project, SpringProcessLiveData[] processLiveData) {
		return provideHover(annotation, doc, processLiveData);
	}

	@Override
	public Collection<CodeLens> getLiveHintCodeLenses(IJavaProject project, Annotation annotation, TextDocument doc, SpringProcessLiveData[] processLiveData) {
		try {
			Optional<List<LiveConditional>> val = getMatchedLiveConditionals(annotation, processLiveData);
			if (val.isPresent()) {
				org.openrewrite.marker.Range r = ORAstUtils.getRange(annotation);
				Range hoverRange = doc.toRange(r.getStart().getOffset(), r.length());
				return ImmutableList.of(new CodeLens(hoverRange));
			}
		} catch (Exception e) {
			log.error("", e);
		}

		return null;
	}

	private Optional<List<LiveConditional>> getMatchedLiveConditionals(Annotation annotation,
			SpringProcessLiveData[] processLiveData) throws Exception {
		if (processLiveData != null) {
			List<LiveConditional> all = new ArrayList<>();

			for (SpringProcessLiveData liveData : processLiveData) {
				LiveConditional[] conditionals = liveData.getLiveConditionals();
				if (conditionals != null) {
					Arrays.stream(conditionals).forEach((conditional) -> {
						if (matchesAnnotation(annotation, conditional)) {
							all.add(conditional);
						}
					});
				};
			}
			if (!all.isEmpty()) {
				return Optional.of(all);
			}
		}
		return Optional.empty();
	}

	private Hover provideHover(Annotation annotation, TextDocument doc,
			SpringProcessLiveData[] processLiveData) {

		try {
			List<Either<String, MarkedString>> hoverContent = new ArrayList<>();
			Optional<List<LiveConditional>> val = getMatchedLiveConditionals(annotation, processLiveData);

			if (val.isPresent()) {
				addHoverContent(val.get(), hoverContent);
			}

			org.openrewrite.marker.Range r = ORAstUtils.getRange(annotation);
			Range hoverRange = doc.toRange(r.getStart().getOffset(), r.length());
			Hover hover = new Hover();

			hover.setContents(hoverContent);
			hover.setRange(hoverRange);

			return hover;
		} catch (Exception e) {
			log.error("", e);
		}

		return null;
	}

	private void addHoverContent(List<LiveConditional> conditionals, List<Either<String, MarkedString>> hoverContent)
			throws Exception {
		for (int i = 0; i < conditionals.size(); i++) {
			LiveConditional conditional = conditionals.get(i);
			hoverContent.add(Either.forLeft(conditional.getMessage()));
			hoverContent.add(Either.forLeft(LiveHoverUtils.niceAppName(conditional.getProcessId(), conditional.getProcessName())));
		}
	}

	/**
	 *
	 * @param annotation
	 * @param jsonKey
	 * @return true if the annotation matches the information in the json key from
	 *         the running app.
	 */
	protected boolean matchesAnnotation(Annotation annotation, LiveConditional liveConditional) {

		// First check that the annotation matches the live conditional annotation
		FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
		if (type == null) {
			return false;
		}
		
		String annotationName = type.getClassName();
		if (!liveConditional.getMessage().contains(annotationName)) {
			return false;
		}

		// Check that Java type in annotation in editor matches Java information in the live Conditional
		J parent = ORAstUtils.getParent(annotation);
		String typeInfo = liveConditional.getTypeInfo();

		if (parent instanceof MethodDeclaration) {
			MethodDeclaration methodDec = (MethodDeclaration) parent;
			ClassDeclaration declaringType = ORAstUtils.findNode(methodDec, ClassDeclaration.class);
			if (declaringType == null) {
				return false;
			}
			String annotationMethodName = methodDec.getSimpleName();
			return typeInfo.contains(declaringType.getType().getClassName()) && typeInfo.contains(annotationMethodName);
		} else if (parent instanceof ClassDeclaration) {
			ClassDeclaration typeDec = (ClassDeclaration) parent;
			String annotationDeclaringClassName = typeDec.getType().getClassName();
			return typeInfo.contains(annotationDeclaringClassName);
		}
		return false;
	}

}
