/*******************************************************************************
 * Copyright (c) 2026 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.jdt.refactoring;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.springframework.ide.vscode.boot.java.utils.ModelAttributeNames;

/**
 * A JDT-based refactoring that removes the explicit name from {@code @ModelAttribute} method
 * parameters, e.g. turning {@code @ModelAttribute("user") User account} into
 * {@code @ModelAttribute User account}. Only the {@code value} or {@code name} attribute is removed,
 * any other attribute such as {@code binding} is kept.
 * <p>
 * The source may have changed since a {@link Fix} was computed, e.g. the parameter type, so each
 * name is checked again against the current source with {@link ModelAttributeNames#findRedundantName(Annotation)} and
 * only removed if it is still the redundant name of the {@link Fix}. If bindings can't be resolved,
 * the annotation is left untouched.
 * <p>
 * A single {@link Fix} backs the quickfix for a single annotation, passing all of them found in a
 * file backs the "fix all in file" quickfix.
 *
 * @author Nihal Kumar Sharma
 */
public class RemoveModelAttributeNameRefactoring implements JdtRefactoring {

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
			StringLiteral name = annotation != null ? ModelAttributeNames.findRedundantName(annotation) : null;
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

}
