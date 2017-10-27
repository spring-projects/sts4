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
package org.springframework.ide.vscode.boot.java.conditionals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ide.vscode.boot.java.handlers.HoverProvider;
import org.springframework.ide.vscode.boot.java.utils.HoverContentUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 *
 * Provides live hovers and hints for @ConditionalOn... Spring Boot annotations
 * from running spring boot apps.
 */
public class ConditionalsLiveHoverProvider implements HoverProvider {

	@Override
	public CompletableFuture<Hover> provideHover(ASTNode node, Annotation annotation, ITypeBinding type, int offset,
			TextDocument doc, SpringBootApp[] runningApps) {
		return provideHover(annotation, doc, runningApps);
	}

	@Override
	public Collection<Range> getLiveHoverHints(Annotation annotation, TextDocument doc, SpringBootApp[] runningApps) {
		try {
			if (runningApps.length > 0) {
				ConditionalParserFromRunningApp parser = new ConditionalParserFromRunningApp();
				Optional<List<RunningAppConditional>> val = parser.parse(annotation, runningApps);
				if (val.isPresent()) {
					Range hoverRange = doc.toRange(annotation.getStartPosition(), annotation.getLength());
					return ImmutableList.of(hoverRange);
				}
			}
		} catch (BadLocationException e) {
			Log.log(e);
		}

		return null;
	}

	private CompletableFuture<Hover> provideHover(Annotation annotation, TextDocument doc,
			SpringBootApp[] runningApps) {

		try {
			List<Either<String, MarkedString>> hoverContent = new ArrayList<>();
			ConditionalParserFromRunningApp parser = new ConditionalParserFromRunningApp();
			Optional<List<RunningAppConditional>> val = parser.parse(annotation, runningApps);

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

	private void addHoverContent(List<RunningAppConditional> conditions,
			List<Either<String, MarkedString>> hoverContent) throws Exception {
		for (int i = 0; i < conditions.size(); i++) {
			RunningAppConditional condition = conditions.get(i);
			hoverContent.add(Either.forLeft(condition.message));
			hoverContent.add(Either
					.forLeft(HoverContentUtils.getProcessInformation(condition.app)));

			if (i < conditions.size() - 1) {
				hoverContent.add(Either.forLeft("---"));
			}
		}
	}

	/**
	 * Parsers @ConditionalOn annotations from a spring boot running app's
	 * autoconfig report. An example of a conditional that is parsed from a running
	 * app's autoconfig report would be:
	 *
	 * {"TraceRepositoryAutoConfiguration#traceRepository":[{"condition":"OnBeanCondition","message":"@ConditionalOnMissingBean
	 * (types: org.springframework.boot.actuate.trace.TraceRepository;
	 * SearchStrategy: all) did not find any beans"}]
	 *
	 */
	public static class ConditionalParserFromRunningApp {

		public Optional<List<RunningAppConditional>> parse(Annotation annotation, SpringBootApp[] runningApps) {

			try {
				List<RunningAppConditional> allConditionals = new ArrayList<>();
				for (SpringBootApp app : runningApps) {
					String autoConfigRecord = app.getAutoConfigReport();
					if (autoConfigRecord != null) {
						JSONObject autoConfigJson = new JSONObject(autoConfigRecord);
						List<RunningAppConditional> conditionalsFromPositiveMatches = getConditionals(app, annotation,
								autoConfigJson);
						if (!conditionalsFromPositiveMatches.isEmpty()) {
							allConditionals.addAll(conditionalsFromPositiveMatches);
						}
					}
				}
				if (!allConditionals.isEmpty()) {
					return Optional.of(allConditionals);
				}
			} catch (Exception e) {
				Log.log(e);
			}
			return Optional.empty();
		}

		/**
		 *
		 * @param app
		 * @param annotation
		 * @param autoConfigJson
		 * @return non-null list of conditionals parsed from an autoconfig report. List
		 *         may be empty.
		 */
		private List<RunningAppConditional> getConditionals(SpringBootApp app, Annotation annotation,
				JSONObject autoConfigJson) {
			List<RunningAppConditional> conditions = new ArrayList<>();

			getPositiveMatches(autoConfigJson).ifPresent((positiveMatches) -> {
				Iterator<String> pMKeys = positiveMatches.keys();
				while (pMKeys.hasNext()) {
					String positiveMatchKey = pMKeys.next();
					if (matchesAnnotation(annotation, positiveMatchKey)) {
						JSONArray matchList = (JSONArray) positiveMatches.get(positiveMatchKey);
						matchList.forEach((match) -> {
							if (match instanceof JSONObject) {
								getMatchedCondition(app, (JSONObject) match, annotation)
										.ifPresent((condition) -> conditions.add(condition));
							}
						});
					}
				}
			});

			return conditions;
		}

		protected Optional<JSONObject> getPositiveMatches(JSONObject autoConfigJson) {

			Iterator<String> keys = autoConfigJson.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				if ("positiveMatches".equals(key)) {
					Object obj = autoConfigJson.get(key);
					if (obj instanceof JSONObject) {
						return Optional.of((JSONObject) obj);
					}
				}
			}
			return Optional.empty();
		}

		/**
		 *
		 * @param annotation
		 * @param jsonKey
		 * @return true if the annotation matches the information in the json key from
		 *         the running app.
		 */
		protected boolean matchesAnnotation(Annotation annotation, String jsonKey) {

			ASTNode parent = annotation.getParent();
			if (parent instanceof MethodDeclaration) {
				MethodDeclaration methodDec = (MethodDeclaration) parent;
				IMethodBinding binding = methodDec.resolveBinding();
				String annotationDeclaringClassName = binding.getDeclaringClass().getName();
				String annotationMethodName = binding.getName();
				return jsonKey.contains(annotationDeclaringClassName) && jsonKey.contains(annotationMethodName);
			} else if (parent instanceof TypeDeclaration) {
				TypeDeclaration typeDec = (TypeDeclaration) parent;
				String annotationDeclaringClassName = typeDec.resolveBinding().getName();
				return jsonKey.contains(annotationDeclaringClassName);
			}
			return false;
		}

		protected Optional<RunningAppConditional> getMatchedCondition(SpringBootApp app, JSONObject conditionJson,
				Annotation annotation) {
			if (conditionJson != null) {
				String condition = (String) conditionJson.get("condition");
				String message = (String) conditionJson.get("message");
				String annotationName = annotation.resolveTypeBinding().getName();
				if (message.contains(annotationName)) {
					return Optional.of(new RunningAppConditional(app, condition, message));
				}
			}
			return Optional.empty();
		}
	}

	public static class RunningAppConditional {

		public final String condition;
		public final String message;
		public final SpringBootApp app;

		public RunningAppConditional(SpringBootApp app, String condition, String message) {
			this.condition = condition;
			this.message = message;
			this.app = app;
		}
	}
}
