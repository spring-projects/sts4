/*******************************************************************************
 * Copyright (c) 2026 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.jdt.refactoring;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A JDT-based refactoring that removes the explicit name from {@code @ModelAttribute} method
 * parameters, e.g. turning {@code @ModelAttribute("user") User account} into
 * {@code @ModelAttribute User account}. Only the {@code value} or {@code name} attribute is removed,
 * any other attribute such as {@code binding} is kept.
 * <p>
 * The source may have changed since a {@link Fix} was computed, e.g. the parameter type, so each
 * name is checked again against the current source with {@link #findRedundantName(Annotation)} and
 * only removed if it is still the redundant name of the {@link Fix}. If bindings can't be resolved,
 * the annotation is left untouched.
 * <p>
 * A single {@link Fix} backs the quickfix for a single annotation, passing all of them found in a
 * file backs the "fix all in file" quickfix.
 *
 * @author Nihal Kumar Sharma
 */
public class RemoveModelAttributeNameRefactoring implements JdtRefactoring {

	// Spring pluralizes the default name for collections and appends a suffix for reactive types
	private static final Set<String> COLLECTION_AND_REACTIVE_TYPES = Set.of(
			"java.util.Collection",
			"java.util.concurrent.CompletionStage",
			"java.util.concurrent.Flow.Publisher",
			"org.reactivestreams.Publisher");

	/**
	 * @param annotationOffset start position of the {@code @ModelAttribute} annotation
	 * @param name             the redundant name to remove
	 */
	public record Fix(int annotationOffset, String name) {
	}

	private final List<Fix> fixes;

	public RemoveModelAttributeNameRefactoring(Fix... fixes) {
		this(List.of(fixes));
	}

	public RemoveModelAttributeNameRefactoring(List<Fix> fixes) {
		this.fixes = fixes;
	}

	@Override
	public void apply(ASTRewrite rewrite, CompilationUnit cu) {
		AST ast = cu.getAST();
		for (Fix fix : fixes) {
			Annotation annotation = JdtRefactorUtils.findAncestorAtOffset(cu, fix.annotationOffset(), Annotation.class);
			StringLiteral name = annotation != null ? findRedundantName(annotation) : null;
			if (name == null || !name.getLiteralValue().equals(fix.name())) {
				continue;
			}

			if (annotation instanceof NormalAnnotation na && na.values().size() > 1) {
				rewrite.getListRewrite(na, NormalAnnotation.VALUES_PROPERTY).remove(name.getParent(), null);
			} else {
				rewrite.replace(annotation, JdtRefactorUtils.markerAnnotationLike(ast, annotation), null);
			}
		}
	}

	/**
	 * Returns the explicit name of the given annotation if it is Spring's {@code @ModelAttribute} on a
	 * method parameter and the name is the default Spring derives from the parameter type anyway, or
	 * {@code null} otherwise, including when the annotation or the parameter type can't be resolved.
	 */
	public static StringLiteral findRedundantName(Annotation annotation) {
		ITypeBinding annotationType = annotation.resolveTypeBinding();
		if (annotationType == null || annotationType.isRecovered() || !Annotations.MODEL_ATTRIBUTE.equals(annotationType.getQualifiedName())) {
			return null;
		}
		if (!(annotation.getParent() instanceof SingleVariableDeclaration parameter)
				|| !(parameter.getParent() instanceof MethodDeclaration method) || method.isConstructor()) {
			return null;
		}
		return findName(annotation) instanceof StringLiteral literal && literal.getLiteralValue().equals(getDefaultName(parameter.resolveBinding()))
				? literal
				: null;
	}

	/**
	 * Returns the explicit name of the given annotation, i.e. its single member value or its
	 * {@code value} or {@code name} attribute, or {@code null} if there is none or both are set.
	 */
	private static Expression findName(Annotation annotation) {
		if (annotation instanceof SingleMemberAnnotation sma) {
			return sma.getValue();
		}
		Expression name = null;
		if (annotation instanceof NormalAnnotation na) {
			for (Object value : na.values()) {
				MemberValuePair pair = (MemberValuePair) value;
				String attribute = pair.getName().getIdentifier();
				if ("value".equals(attribute) || "name".equals(attribute)) {
					if (name != null) {
						return null;
					}
					name = pair.getValue();
				}
			}
		}
		return name;
	}

	/**
	 * Returns the name Spring uses for a {@code @ModelAttribute} parameter that doesn't declare one
	 * (see {@code ModelFactory.getNameForParameter}), or {@code null} if it can't be derived reliably.
	 * Only plain class types are supported: arrays, collections and reactive types get different
	 * names, and generic types as well as types with unresolved parts in their hierarchy are skipped.
	 */
	private static String getDefaultName(IVariableBinding parameter) {
		ITypeBinding type = parameter != null ? parameter.getType() : null;
		if (type == null || !(type.isClass() || type.isInterface() || type.isEnum() || type.isRecord())
				|| type.isParameterizedType() || type.isRawType() || type.getBinaryName() == null) {
			return null;
		}

		for (Iterator<ITypeBinding> itr = ASTUtils.getHierarchyTypesBreadthFirstIterator(type); itr.hasNext();) {
			ITypeBinding hierarchyType = itr.next();
			if (hierarchyType.isRecovered() || COLLECTION_AND_REACTIVE_TYPES.contains(hierarchyType.getErasure().getQualifiedName())) {
				return null;
			}
		}

		// same as ClassUtils.getShortNameAsProperty(Class), which Spring uses for the default name
		String shortName = ClassUtils.getShortName(type.getBinaryName());
		return StringUtils.uncapitalizeAsProperty(shortName.substring(shortName.lastIndexOf('.') + 1));
	}

}
