/*******************************************************************************
 * Copyright (c) 2017, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * Utility class for working with annotation and discovering / understanding their
 * 'inheritance' structure.
 * <p>
 * Provides methods to ask questions about inheritance between annotations.
 *
 * @author Kris De Volder
 * @author Alex Boyko
 * @author Martin Lippert
 */
public class AnnotationHierarchies {
	
	private static final Logger log = LoggerFactory.getLogger(AnnotationHierarchies.class);
	
	private static final String CU_PROPERTY_KEY = AnnotationHierarchies.class.getName();
	
	private final ConcurrentHashMap<String, AnnotationTypeInformation> cache;
	private final Predicate<ITypeBinding> filter;

	@SuppressWarnings("unchecked")
	public static <T extends AnnotationHierarchies> T get(ASTNode n) {
		return (T) n.getRoot().getProperty(CU_PROPERTY_KEY);
	}
	
	public static boolean set(ASTNode n, AnnotationHierarchies annotations) {
		ASTNode root = n.getRoot();
		AnnotationHierarchies a = get(root);
		if (a == null) {
			root.setProperty(CU_PROPERTY_KEY, annotations);
			return true;
		}
		return false;
	}
	
	public AnnotationHierarchies(Predicate<ITypeBinding> filter) {
		this.cache = new ConcurrentHashMap<>();
		this.filter = filter;
	}
	
	public AnnotationHierarchies() {
		this(tb -> !tb.getKey().startsWith("Ljava/")); //mostly intended to capture java.lang.annotation.* types. But really it should be
		//safe to ignore any type defined by the JRE since it can't possibly be inheriting from a spring annotation.
	}
	
	/**
	 * Computes annotations only annotating this binding.
	 * 
	 * @param binding binding
	 * @return list of annotations
	 */
	// this lock is used to protect multi-threaded access to this helper class
	// due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=571247
	synchronized List<IAnnotationBinding> getDirectSuperAnnotationBindings(IBinding binding) {
		try {
			if (binding != null) {
				IAnnotationBinding[] annotations = binding.getAnnotations();
				if (annotations.length != 0) {
					ImmutableList.Builder<IAnnotationBinding> superAnnotations = ImmutableList.builder();
					for (IAnnotationBinding ab : annotations) {
						ITypeBinding sa = ab.getAnnotationType();
						if (sa != null) {
							if (accept(sa)) {
								superAnnotations.add(ab);
							}
						}
					}
					return superAnnotations.build();
				}
			}
		}
		catch (AbortCompilation e) {
			log.debug("compilation aborted ", e);
			// ignore this, it is most likely caused by broken source code, a broken classpath, or some optional dependencies not being on the classpath
		}
	
		return ImmutableList.of();
	}
	
	private boolean accept(ITypeBinding tb) {
		return filter == null || filter.test(tb);
	}

	/**
	 * Produces the iterator over all annotations hierarchy for the binding. If the
	 * binding happens to be {@link IAnnotationBinding} it'll be included in the
	 * hierarchy
	 * 
	 * @param binding the binding
	 * @return iterator over annotations hierarchy
	 */
	public Iterator<IAnnotationBinding> iterator(IBinding binding) {
		return new Iterator<>() {
			
			private HashSet<String> seen = new HashSet<>();
			private Queue<IAnnotationBinding> queue = new LinkedList<>();

			{
				if (binding instanceof IAnnotationBinding ab) {
					seen.add(ab.getAnnotationType().getKey());
					queue.add(ab);
				} else {
					if (binding instanceof ITypeBinding tb && tb.isAnnotation()) {
						seen.add(tb.getKey());
					}
					for (IAnnotationBinding ab :  getDirectSuperAnnotationBindings(binding)) {
						seen.add(ab.getAnnotationType().getKey());
						queue.add(ab);
					}
				}
			}

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public IAnnotationBinding next() {
				IAnnotationBinding next = queue.poll();
				for (IAnnotationBinding a : getDirectSuperAnnotationBindings(next.getAnnotationType())) {
					String key = a.getAnnotationType().getKey();
					if (!seen.contains(key)) {
						seen.add(key);
						queue.add(a);
					}
				}
				return next;
			}
			
		};
	}
	
	/**
	 * The list of all annotations within the hierarchy.
	 * Note: for searching purposes it is best to use {@link #iterator(IBinding)}
	 * 
	 * @param binding
	 * @return the list of annotations in the hierarchy
	 */
	public List<IAnnotationBinding> getAllAnnotations(IBinding binding) {
		if (binding != null) {
			List<IAnnotationBinding> annotations = new ArrayList<>();
			for (Iterator<IAnnotationBinding> itr = iterator(binding); itr.hasNext();) {
				annotations.add(itr.next());
			}
			return annotations;
		}
		return Collections.emptyList();
	}
	
	private Optional<AnnotationTypeInformation> annotationInfo(ITypeBinding typeBinding) {
		if (accept(typeBinding)) {
			return Optional.of(cache.computeIfAbsent(typeBinding.getKey(), s -> compute(typeBinding)));
		}
		return Optional.empty();
	}
	
	private AnnotationTypeInformation compute(ITypeBinding typeBinding) {
		Set<String> inherited = new LinkedHashSet<>();
		String fqn = typeBinding.getQualifiedName();
		// Add itself to the set to flag it as been seen
		inherited.add(fqn);
		collectInheritedAnnotations(typeBinding, inherited);
		// remove itself from `inherited` set as we'd like to leave only inherited annotations FQNs 
		inherited.remove(fqn);
		return new AnnotationTypeInformation(fqn, inherited);
	}
	
	
	// recursively collect annotations of the given annotation type
	private void collectInheritedAnnotations(ITypeBinding annotationType, Set<String> inherited) {
		for (IAnnotationBinding annotation : getDirectSuperAnnotationBindings(annotationType)) {
			ITypeBinding type = annotation.getAnnotationType();
			boolean notSeenYet = inherited.add(type.getQualifiedName());
			if (notSeenYet) {
				collectInheritedAnnotations(type, inherited);
			}
		}
	}

	public boolean isAnnotatedWith(IBinding binding, Predicate<String> annotationFqnTest) {
		if (binding instanceof IAnnotationBinding ab) {
			return annotationInfo(ab.getAnnotationType()).map(info -> info.inherits(annotationFqnTest)).orElse(false);
		} else if (binding instanceof ITypeBinding tb && tb.isAnnotation()) {
			return annotationInfo(tb).map(info -> info.inherits(annotationFqnTest)).orElse(false);
		} else {
			for (IAnnotationBinding ab : getDirectSuperAnnotationBindings(binding)) {
				if (isAnnotatedWith(ab.getAnnotationType(), annotationFqnTest)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isAnnotatedWith(IBinding binding, String annotationTypeFqn) {
		if (binding instanceof IAnnotationBinding ab) {
			return annotationInfo(ab.getAnnotationType()).map(info -> info.inherits(annotationTypeFqn)).orElse(false);
		} else if (binding instanceof ITypeBinding tb && tb.isAnnotation()) {
			return annotationInfo(tb).map(info -> info.inherits(annotationTypeFqn)).orElse(false);
		} else {
			for (IAnnotationBinding ab : getDirectSuperAnnotationBindings(binding)) {
				if (isAnnotatedWith(ab.getAnnotationType(), annotationTypeFqn)) {
					return true;
				}
			}
		}
		return false;
	}
	
}
