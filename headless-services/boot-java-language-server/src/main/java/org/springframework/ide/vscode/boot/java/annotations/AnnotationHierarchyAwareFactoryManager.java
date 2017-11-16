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

import org.eclipse.jdt.core.dom.ITypeBinding;

import com.google.common.collect.ImmutableList;

import reactor.util.function.Tuple2;

/**
 * @author Kris De Volder
 */
public class AnnotationHierarchyAwareFactoryManager<T> {

	@FunctionalInterface
	public interface Factory<T> {
		T create(String fqAnnotationType);
	}

	private AnnotationHierarchyAwareLookup<Factory<T>> factories = new AnnotationHierarchyAwareLookup<Factory<T>>();

	/**
	 * Deprecated, for proper handling of annotation inheritance, use putFactory method instead.
	 */
	@Deprecated
	public void put(String fqAnnotationType, T value) {
		factories.put(fqAnnotationType, true, (actualAnnotationType) ->
			actualAnnotationType.equals(fqAnnotationType) ? value : null
		);
	}

	/**
	 * Add a 'base' factory which creates a `T` for a given type of annotation.
	 * The factory will be passed the fq name of the annotation 'base' annotation.
	 * It is acceptable for the Factory to return null. Null values will simply
	 * be ignored.
	 */
	public void putFactory(String fqAnnotationType, Factory<T> factory) {
		factories.put(fqAnnotationType, false, factory);
	}

	public Collection<T> get(ITypeBinding typeBinding) {
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		for (Tuple2<String, Factory<T>> entry : factories.get(typeBinding)) {
			String superAnnotationName = entry.getT1();
			Factory<T> factory = entry.getT2();
			T element = factory.create(superAnnotationName);
			if (element!=null) {
				builder.add(element);
			}
		}
		return builder.build();
	}

}
