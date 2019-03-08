/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.springframework.ide.vscode.commons.util.Assert;

import com.google.common.collect.ImmutableList;

/**
 * A Map-like utilty that allows putting and getting values associated with
 * annotation types.
 * <p>
 * The lookup is 'hierarchy aware' which means that is able to associate values
 * with a given type and all its subtypes all at once.
 *
 * @author Kris De Volder
 */
public class AnnotationHierarchyAwareLookup<T> {

	private static class Binding<T> {
		T value;
		boolean isOverriding;
		public Binding(T value, boolean isOverriding) {
			this.isOverriding = isOverriding;
			this.value = value;
		}
	}

	/**
	 * Associates fq anotation type name to a Binding.
	 */
	private Map<String, Binding<T>> bindings = new HashMap<>();

	/**
	 * Associates a value with a given annotation type (and all its subtypes implicitly).
	 *
	 * @param fqName Fully qualified type name for the annotation.
	 * @param overrideSuperTypes Determines whether the binding has 'override' behavior. Override behavior
	 *                           means that this binding stops the search for additional bindings associated
	 *                           with a super type. If override behavior is disabled the search will continue
	 *                           so that values associated with supertypes will also be found and returned
	 *                           in addition to the more specific binding.
	 * @param value
	 */
	public void put(String fqName,  boolean overrideSuperTypes, T value) {
		Assert.isLegal(bindings.get(fqName)==null, "Multiple bindings to the same fqName are not supported");
		bindings.put(fqName, new Binding<>(value, overrideSuperTypes));
	}

	/**
	 * Gets all associations applicable to a given annotationType. Note that a single 'put' binding for
	 * a supertype can result in mutiple applicable associations for single annotation type because
	 * a single put actually creates associations for a type and all its subtypes implicitly.
	 * <p>
	 * So, for example:
	 * <code>
	 *    AnnotationHierarchyAwareLookup registry = new AnnotationHierarchyAwareLookup<String>();
	 *    registry.put("spring.annotation.Component", "ComponentProvider");
	 * </code>
	 * Now, assuming that RestController is a sub annotation of Controller which is a subtype of Component,
	 * then if we call `get(...typeBinding of RestController...`) we will get back a collection of 3 elements:
	 * ("spring.annotation.Component", "ComponentProvider"),
	 * ("spring.annotation.Controller", "ComponentProvider")
	 * ("spring.annotation.RestController", "ComponentProvider")
	 * <p>
	 * This reflects the fact that a binding for ComponentProvider also can function as a provider for Controller
	 * and RestController; and that RestController in turn can be interpreted as a specialized Component or Controller.
	 * Therefore we would expect in a situation where a concrete annotation of type RestController is found in the AST,
	 * a symbol provider for Components should be asked to produce symbols for Component, Controller and RestController,
	 * so should result in 3 separate calls to the symbols provider.
	 */
	public Collection<T> get(ITypeBinding annotationType) {
		ImmutableList.Builder<T> found = ImmutableList.builder();
		findElements(annotationType, new LinkedHashSet<>(), found::add);
		return found.build();
	}

	public Collection<T> getAll() {
		ImmutableList.Builder<T> found = ImmutableList.builder();
		Collection<Binding<T>> values = bindings.values();
		values.forEach(binding -> found.add(binding.value));
		return found.build();
	}

	private void findElements(ITypeBinding typeBinding, HashSet<String> seen, Consumer<T> requestor) {
		String qname = typeBinding.getQualifiedName();
		if (seen.add(qname)) {
			Binding<T> binding = bindings.get(qname);
			boolean isOverriding = false;
			if (binding!=null) {
				requestor.accept(binding.value);
				isOverriding = binding.isOverriding;
			}
			if (!isOverriding) {
				for (ITypeBinding superAnnotation : AnnotationHierarchies.getDirectSuperAnnotations(typeBinding)) {
					findElements(superAnnotation, seen, requestor);
				}
			}
		}
	}

	public void put(String annotationName, T value) {
		put(annotationName, true, value);
	}

	public boolean containsKey(String fqName) {
		return bindings.containsKey(fqName);
	}

//	private static int indent = 0;
//
//	private static void debug(int indent, String msg) {
//		for (int i = 0; i < indent; i++) {
//			System.out.print("  ");
//		}
//		System.out.println(msg);
//	}
//	private static int debug_in(String msg) {
//		for (int i = 0; i < indent; i++) {
//			System.out.print("  ");
//		}
//		System.out.println(">> "+msg);
//		return indent ++;
//	}
//	private static void debug_out(String msg) {
//		indent --;
//		for (int i = 0; i < indent; i++) {
//			System.out.print("  ");
//		}
//		System.out.println("<< "+msg);
//	}

}
