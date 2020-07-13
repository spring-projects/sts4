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

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
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

	public void visit(SingleMemberAnnotation node, ITypeBinding typeBinding, IProblemCollector problemCollector) {
		if (this.paramName != null) {
			return;
		}
		
		Set<String> allAnnotations = AnnotationHierarchies.getTransitiveSuperAnnotations(typeBinding);
		if (!allAnnotations.contains(this.annotationType)) {
			return;
		}
		
		Expression valueExp = node.getValue();

		if (valueExp instanceof StringLiteral) {
			reconcileStringLiteral((StringLiteral) valueExp, problemCollector);
		}
	}

	public void visit(NormalAnnotation node, ITypeBinding typeBinding, IProblemCollector problemCollector) {
		if (paramName == null) {
			return;
		}
		
		Set<String> allAnnotations = AnnotationHierarchies.getTransitiveSuperAnnotations(typeBinding);
		if (!allAnnotations.contains(this.annotationType)) {
			return;
		}
			
		List<?> values = node.values();
		
		for (Object value : values) {
			if (value instanceof MemberValuePair) {
				MemberValuePair pair = (MemberValuePair) value;
				String name = pair.getName().getFullyQualifiedName();
				if (name != null && name.equals(paramName)) {
					Expression expression = pair.getValue();
					if (expression instanceof StringLiteral) {
						reconcileStringLiteral((StringLiteral) expression, problemCollector);
					}
				}
			}
		}
	}

	private void reconcileStringLiteral(StringLiteral valueExp, IProblemCollector problemCollector) {
		String value = valueExp.getLiteralValue();
		
		if (value != null && value.startsWith(paramValuePrefix) && value.endsWith(paramValuePostfix)) {
			String valueToReconcile = value.substring(paramValuePrefix.length(), value.length() - paramValuePostfix.length());
			
			reconciler.reconcile(valueToReconcile, valueExp.getStartPosition() + paramValuePrefix.length() + 1, problemCollector);
		}
	}

}
