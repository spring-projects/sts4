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
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;

public final class AnnotationParamSpelExtractor {
	
	private static final String SPRING_CACHEABLE = "org.springframework.cache.annotation.Cacheable";
	private static final String SPRING_CACHE_EVICT = "org.springframework.cache.annotation.CacheEvict";
	
	private static final String SPRING_EVENT_LISTENER = "org.springframework.context.event.EventListener";
	
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

			new AnnotationParamSpelExtractor(SPRING_EVENT_LISTENER, "condition", "", ""),
			
			new AnnotationParamSpelExtractor(SPRING_PRE_AUTHORIZE, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_PRE_AUTHORIZE, "value", "", ""),
			new AnnotationParamSpelExtractor(SPRING_PRE_FILTER, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_PRE_FILTER, "value", "", ""),
			new AnnotationParamSpelExtractor(SPRING_POST_AUTHORIZE, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_POST_AUTHORIZE, "value", "", ""),
			new AnnotationParamSpelExtractor(SPRING_POST_FILTER, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_POST_FILTER, "value", "", ""),

			new AnnotationParamSpelExtractor(SPRING_CONDITIONAL_ON_EXPRESSION, null, "", ""),
			new AnnotationParamSpelExtractor(SPRING_CONDITIONAL_ON_EXPRESSION, "value", "", ""),
			
			new AnnotationParamSpelExtractor(Annotations.SCHEDULED, "cron", "#{", "}"),
	};
	
	
	public record Snippet(String text, int offset) {}
	
	private final String annotationType;
	private final String paramName;
	private final String paramValuePrefix;
	private final String paramValuePostfix;
	
	public AnnotationParamSpelExtractor(String annotationType, String paramName, String paramValuePrefix,
			String paramValuePostfix) {
		this.annotationType = annotationType;
		this.paramName = paramName;
		this.paramValuePrefix = paramValuePrefix;
		this.paramValuePostfix = paramValuePostfix;
	}

	public Optional<Snippet> getSpelRegion(NormalAnnotation a) {
		if (paramName == null) {
			return Optional.empty();
		}
		
		if (!AnnotationHierarchies.hasTransitiveSuperAnnotationType(a.resolveTypeBinding(), this.annotationType)) {
			return Optional.empty();
		}
			
		List<?> values = a.values();
		
		for (Object value : values) {
			if (value instanceof MemberValuePair) {
				MemberValuePair pair = (MemberValuePair) value;
				String name = pair.getName().getFullyQualifiedName();
				if (name != null && name.equals(paramName)) {
					Expression expression = pair.getValue();
					if (expression instanceof StringLiteral) {
						return fromStringLiteral((StringLiteral) expression);
					}
				}
			}
		}
		
		return Optional.empty();
	}
	
	public Optional<Snippet> getSpelRegion(SingleMemberAnnotation a) {
		if (this.paramName != null) {
			return Optional.empty();
		}
		
		if (!AnnotationHierarchies.hasTransitiveSuperAnnotationType(a.resolveTypeBinding(), this.annotationType)) {
			return Optional.empty();
		}
		
		Expression valueExp = a.getValue();

		if (valueExp instanceof StringLiteral) {
			return fromStringLiteral((StringLiteral) valueExp);
		}
		
		return Optional.empty();
	}
	
	private Optional<Snippet> fromStringLiteral(StringLiteral valueExp) {
		String value = valueExp.getEscapedValue();
		value = value.substring(1, value.length() - 1);
		if (value != null) {
			int startIdx = value.indexOf(paramValuePrefix);
			if (startIdx >= 0) {
				int endIdx = value.lastIndexOf(paramValuePostfix);
				if (endIdx >= 0) {
					String spelText = value.substring(startIdx + paramValuePrefix.length(), endIdx);
					int offset = valueExp.getStartPosition() + startIdx + paramValuePrefix.length() + 1;
					return Optional.of(new Snippet(spelText, offset));
				}
			}
		}
		return Optional.empty();
	}


}
