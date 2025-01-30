/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.spel;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.embedded.lang.EmbeddedLangAstUtils;
import org.springframework.ide.vscode.boot.java.embedded.lang.EmbeddedLanguageSnippet;
import org.springframework.ide.vscode.boot.java.embedded.lang.EmbeddedLanguageSnippetWithPrefixAndSuffix;

public final class AnnotationParamSpelExtractor {
	
	private static final String SPRING_CACHEABLE = "org.springframework.cache.annotation.Cacheable";
	private static final String SPRING_CACHE_EVICT = "org.springframework.cache.annotation.CacheEvict";
	
	private static final String SPRING_PRE_AUTHORIZE = "org.springframework.security.access.prepost.PreAuthorize";
	private static final String SPRING_PRE_FILTER = "org.springframework.security.access.prepost.PreFilter";
	private static final String SPRING_POST_AUTHORIZE = "org.springframework.security.access.prepost.PostAuthorize";
	private static final String SPRING_POST_FILTER= "org.springframework.security.access.prepost.PostFilter";
	
	private static final String SPRING_CONDITIONAL_ON_EXPRESSION = "org.springframework.boot.autoconfigure.condition.ConditionalOnExpression";
	
	public final static AnnotationParamSpelExtractor[] SPEL_EXTRACTORS = new AnnotationParamSpelExtractor[] {
			new AnnotationParamSpelExtractor(Annotations.VALUE, null, "#{", "}"),
			new AnnotationParamSpelExtractor(Annotations.VALUE, "value", "#{", "}"),

			new AnnotationParamSpelExtractor(SPRING_CACHEABLE, "key", "", ""),
			new AnnotationParamSpelExtractor(SPRING_CACHEABLE, "condition", "", ""),
			new AnnotationParamSpelExtractor(SPRING_CACHEABLE, "unless", "", ""),

			new AnnotationParamSpelExtractor(SPRING_CACHE_EVICT, "key", "", ""),
			new AnnotationParamSpelExtractor(SPRING_CACHE_EVICT, "condition", "", ""),

			new AnnotationParamSpelExtractor(Annotations.EVENT_LISTENER, "condition", "", ""),
			
			new AnnotationParamSpelExtractor(SPRING_PRE_AUTHORIZE, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_PRE_AUTHORIZE, "value", "", ""),
			new AnnotationParamSpelExtractor(SPRING_PRE_FILTER, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_PRE_FILTER, "value", "", ""),
			new AnnotationParamSpelExtractor(SPRING_POST_AUTHORIZE, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_POST_AUTHORIZE, "value", "", ""),
			new AnnotationParamSpelExtractor(SPRING_POST_FILTER, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_POST_FILTER, "value", "", ""),

			new AnnotationParamSpelExtractor(SPRING_CONDITIONAL_ON_EXPRESSION, null, "#{", "}", true),
			new AnnotationParamSpelExtractor(SPRING_CONDITIONAL_ON_EXPRESSION, "value", "#{", "}", true),
			
			new AnnotationParamSpelExtractor(Annotations.SCHEDULED, "cron", "#{", "}"),
	};
	
	
	public record Snippet(String text, int offset) {}
	
	public record PrefixSuffix(String prefix, String suffix) {}
	
	private final String annotationType;
	private final String paramName;
	
	private final List<PrefixSuffix> prefixSuffixes;
	
	public AnnotationParamSpelExtractor(String annotationType, String paramName, String paramValuePrefix,
			String paramValueSuffix) {
		this.annotationType = annotationType;
		this.paramName = paramName;
		this.prefixSuffixes = List.of(new PrefixSuffix(paramValuePrefix, paramValueSuffix));
	}

	public AnnotationParamSpelExtractor(String annotationType, String paramName, String paramValuePrefix,
			String paramValueSuffx, boolean optinalPrefixAndSuffix) {
		this.annotationType = annotationType;
		this.paramName = paramName;
		this.prefixSuffixes = List.of(new PrefixSuffix(paramValuePrefix, paramValueSuffx), new PrefixSuffix("",  ""));
	}
	
	public Optional<EmbeddedLanguageSnippet> getSpelRegion(NormalAnnotation a) {
		if (paramName == null) {
			return Optional.empty();
		}
		
		if (!AnnotationHierarchies.get(a).isAnnotatedWith(a.resolveAnnotationBinding(), this.annotationType)) {
			return Optional.empty();
		}
			
		List<?> values = a.values();
		
		for (Object value : values) {
			if (value instanceof MemberValuePair) {
				MemberValuePair pair = (MemberValuePair) value;
				String name = pair.getName().getFullyQualifiedName();
				if (name != null && name.equals(paramName)) {
					Expression expression = pair.getValue();
					EmbeddedLanguageSnippet embeddedSnippet = EmbeddedLangAstUtils.extractEmbeddedExpression(expression);
					if (embeddedSnippet != null) {
						return fromEmbeddedSnippet(embeddedSnippet);
					}
				}
			}
		}
		
		return Optional.empty();
	}
	
	public Optional<EmbeddedLanguageSnippet> getSpelRegion(SingleMemberAnnotation a) {
		if (this.paramName != null) {
			return Optional.empty();
		}
		
		if (!AnnotationHierarchies.get(a).isAnnotatedWith(a.resolveAnnotationBinding(), this.annotationType)) {
			return Optional.empty();
		}
		
		Expression valueExp = a.getValue();
		
		EmbeddedLanguageSnippet embeddedSnippet = EmbeddedLangAstUtils.extractEmbeddedExpression(valueExp);
		if (embeddedSnippet != null) {
			return fromEmbeddedSnippet(embeddedSnippet);
		}

		return Optional.empty();
	}
	
	private Optional<EmbeddedLanguageSnippet> fromEmbeddedSnippet(EmbeddedLanguageSnippet embeddedSnippet) {
		String value = embeddedSnippet.getText();
		if (value != null && !value.isBlank()) {
			for (PrefixSuffix ps : prefixSuffixes) {
				int startIdx = value.indexOf(ps.prefix);
				if (startIdx >= 0) {
					int endIdx = value.lastIndexOf(ps.suffix);
					if (endIdx >= 0) {
						return Optional.of(new EmbeddedLanguageSnippetWithPrefixAndSuffix(embeddedSnippet, startIdx + ps.prefix.length(), endIdx));
					}
				}
			}
		}
		return Optional.empty();
	}


}
