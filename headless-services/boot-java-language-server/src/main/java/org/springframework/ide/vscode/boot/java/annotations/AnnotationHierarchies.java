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

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.google.common.collect.ImmutableList;

/**
 * Utility class for working with annotation and discovering / understanding their
 * 'inheritance' structure.
 * <p>
 * Provides methods to ask questions about inheritance between annotations.

 * @author Kris De Volder
 */
public class AnnotationHierarchies {

	protected boolean ignoreAnnotation(String fqname) {
		return fqname.startsWith("java."); //mostly intended to capture java.lang.annotation.* types. But really it should be
		//safe to ignore any type defined by the JRE since it can't possibly be inheriting from a spring annotation.
	};

	public Collection<ITypeBinding> getDirectSuperAnnotations(ITypeBinding typeBinding) {
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

	public Set<String> getTransitiveSuperAnnotations(ITypeBinding typeBinding) {
		Set<String> seen = new HashSet<>();
		findTransitiveSupers(typeBinding, seen);
		return seen;
	}

	private void findTransitiveSupers(ITypeBinding typeBinding, Set<String> seen) {
		String qname = typeBinding.getQualifiedName();
		if (seen.add(qname)) {
			for (ITypeBinding superBinding : getDirectSuperAnnotations(typeBinding)) {
				findTransitiveSupers(superBinding, seen);
			}
		}
	}


}
