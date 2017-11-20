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
package org.springframework.ide.vscode.boot.java.annotations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

/**
 * Utility class for working with annotation and discovering / understanding their
 * 'inheritance' structure.
 * <p>
 * Provides methods to ask questions about inheritance between annotations.

 * @author Kris De Volder
 */
public abstract class AnnotationHierarchies {

	private AnnotationHierarchies() {
	}

	protected static boolean ignoreAnnotation(String fqname) {
		return fqname.startsWith("java."); //mostly intended to capture java.lang.annotation.* types. But really it should be
		//safe to ignore any type defined by the JRE since it can't possibly be inheriting from a spring annotation.
	};

	public static Collection<ITypeBinding> getDirectSuperAnnotations(ITypeBinding typeBinding) {
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		if (annotations!=null && annotations.length!=0) {
			ImmutableList.Builder<ITypeBinding> superAnnotations = ImmutableList.builder();
			for (IAnnotationBinding ab : annotations) {
				ITypeBinding sa = ab.getAnnotationType();
				if (sa!=null) {
					if (!ignoreAnnotation(sa.getQualifiedName())) {
						superAnnotations.add(sa);
					}
				}
			}
			return superAnnotations.build();
		}
		return ImmutableList.of();
	}

	public static Set<String> getTransitiveSuperAnnotations(ITypeBinding typeBinding) {
		Set<String> seen = new HashSet<>();
		findTransitiveSupers(typeBinding, seen);
		return seen;
	}

	public static Stream<String> findTransitiveSupers(ITypeBinding typeBinding, Set<String> seen) {
		String qname = typeBinding.getQualifiedName();
		if (seen.add(qname)) {
			return Stream.concat(
				Stream.of(qname),
				getDirectSuperAnnotations(typeBinding).stream().flatMap(superBinding ->
					findTransitiveSupers(superBinding, seen)
				)
			);
		}
		return Stream.empty();
	}

	public static boolean isSubtypeOf(Annotation annotation, String fqAnnotationTypeName) {
		ITypeBinding annotationType = annotation.resolveTypeBinding();
		if (annotationType!=null) {
			return findTransitiveSupers(annotationType, new HashSet<>())
					.anyMatch(superType -> superType.equals(fqAnnotationTypeName));
		}
		return false;
	}

}
