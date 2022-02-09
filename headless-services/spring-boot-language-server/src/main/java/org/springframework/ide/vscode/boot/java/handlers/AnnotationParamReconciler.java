/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Set;

import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.J.Assignment;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;

/**
 * @author Martin Lippert
 */
public class AnnotationParamReconciler {
	
	private final String annotationType;
	private final String paramName;
	private final String paramValuePrefix;
	private final String paramValuePostfix;
	private final Reconciler reconciler;
	
	public AnnotationParamReconciler(String annotationType, String paramName,
			String paramValuePrefix, String paramValuePostfix, Reconciler reconciler) {
		this.annotationType = annotationType;
		this.paramName = paramName;
		this.paramValuePrefix = paramValuePrefix;
		this.paramValuePostfix = paramValuePostfix;
		this.reconciler = reconciler;
	}

//	public void visit(SingleMemberAnnotation node, ITypeBinding typeBinding, IProblemCollector problemCollector) {
//		if (this.paramName != null) {
//			return;
//		}
//		
//		Set<String> allAnnotations = AnnotationHierarchies.getTransitiveSuperAnnotations(typeBinding);
//		if (!allAnnotations.contains(this.annotationType)) {
//			return;
//		}
//		
//		Expression valueExp = node.getValue();
//
//		if (valueExp instanceof StringLiteral) {
//			reconcileStringLiteral((StringLiteral) valueExp, problemCollector);
//		}
//	}
//
	public void visit(Annotation annotation, IProblemCollector problemCollector) {
		if (paramName == null) {
			return;
		}
		
		Set<String> allAnnotations = AnnotationHierarchies.getTransitiveSuperAnnotations(TypeUtils.asFullyQualified(annotation.getType()));
		if (!allAnnotations.contains(this.annotationType)) {
			return;
		}
			
		for (Expression value : annotation.getArguments()) {
			if (value instanceof Literal) {
				reconcileStringLiteral((Literal) value, problemCollector);
			}
			else if (value instanceof Assignment) {
				Assignment assignment = (Assignment) value;
				String name = assignment.getVariable().printTrimmed();
				if (name != null && name.equals(paramName)) {
					Expression expression = assignment.getAssignment();
					if (expression instanceof Literal) {
						reconcileStringLiteral((Literal) expression, problemCollector);
					}
				}
			}
		}
	}

	private void reconcileStringLiteral(Literal valueExp, IProblemCollector problemCollector) {
		String value = valueExp.printTrimmed();
		
		if (value != null && value.startsWith(paramValuePrefix) && value.endsWith(paramValuePostfix)) {
			String valueToReconcile = value.substring(paramValuePrefix.length(), value.length() - paramValuePostfix.length());
			
			Range r = valueExp.getMarkers().findFirst(Range.class).orElseThrow();
			reconciler.reconcile(valueToReconcile, r.getStart().getOffset() + paramValuePrefix.length() + 1, problemCollector);
		}
	}

}
