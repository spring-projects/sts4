/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.annotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.CollectorUtil;

import com.google.common.collect.ImmutableList;

/**
 * Utility class for working with annotation and discovering / understanding
 * their 'inheritance' structure.
 * <p>
 * Provides methods to ask questions about inheritance between annotations.
 * 
 * @author Kris De Volder
 */
public abstract class AnnotationHierarchies {

	private static final Logger log = LoggerFactory.getLogger(AnnotationHierarchies.class);

	protected static boolean ignoreAnnotation(String fqname) {
		return fqname.startsWith("java."); // mostly intended to capture java.lang.annotation.* types. But really it
											// should be
		// safe to ignore any type defined by the JRE since it can't possibly be
		// inheriting from a spring annotation.
	};

	public static Collection<FullyQualified> getDirectSuperAnnotations(FullyQualified type) {
		try {
			List<FullyQualified> annotations = type.getAnnotations();
			if (annotations != null && !annotations.isEmpty()) {
				ImmutableList.Builder<FullyQualified> superAnnotations = ImmutableList.builder();
				for (FullyQualified ab : annotations) {
					if (!ignoreAnnotation(ab.getFullyQualifiedName())) {
						superAnnotations.add(ab);
					}
				}
				return superAnnotations.build();
			}
		} catch (AbortCompilation e) {
			log.debug("compilation aborted ", e);
			// ignore this, it is most likely caused by broken source code, a broken
			// classpath, or some optional dependencies not being on the classpath
		}

		return ImmutableList.of();
	}

	public static Set<String> getTransitiveSuperAnnotations(FullyQualified type) {
		Set<String> seen = new HashSet<>();
		findTransitiveSupers(type, seen).collect(Collectors.toList());
		return seen;
	}

	public static Stream<FullyQualified> findTransitiveSupers(FullyQualified type, Set<String> seen) {
		String qname = type.getFullyQualifiedName();
		if (seen.add(qname)) {
			return Stream.concat(Stream.of(type), getDirectSuperAnnotations(type).stream()
					.flatMap(superAnnotation -> findTransitiveSupers(superAnnotation, seen)));
		}
		return Stream.empty();
	}

	public static boolean isSubtypeOf(Annotation annotation, String fqAnnotationTypeName) {
		FullyQualified annotationType = TypeUtils.asFullyQualified(annotation.getType());
		if (annotationType != null) {
			return findTransitiveSupers(annotationType, new HashSet<>())
					.anyMatch(superType -> fqAnnotationTypeName.equals(superType.getFullyQualifiedName()));
		}
		return false;
	}

	public static Collection<FullyQualified> getMetaAnnotations(FullyQualified actualAnnotation,
			Predicate<String> isKeyAnnotationName) {
		Stream<FullyQualified> allSupers = findTransitiveSupers(actualAnnotation, new HashSet<>()).skip(1); // Don't
																											// include
																											// 'actualAnnotation'
																											// itself.
		return allSupers.filter(candidate -> isMetaAnnotation(candidate, isKeyAnnotationName))
				.collect(CollectorUtil.toImmutableList());
	}

	private static boolean isMetaAnnotation(FullyQualified candidate, Predicate<String> isKeyAnnotationName) {
		return findTransitiveSupers(candidate, new HashSet<>())
				.anyMatch(sa -> isKeyAnnotationName.test(sa.getFullyQualifiedName()));
	}

}
