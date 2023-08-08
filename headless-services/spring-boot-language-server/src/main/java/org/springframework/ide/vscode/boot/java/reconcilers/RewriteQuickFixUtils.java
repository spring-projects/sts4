/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.openrewrite.Tree;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix.QuickfixData;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class RewriteQuickFixUtils {
	
	public static Range createOpenRewriteRange(CompilationUnit cu, ASTNode node) {
		
		int startOffset = node.getStartPosition();
		int startLine = cu.getLineNumber(startOffset);
		int startColumn = cu.getColumnNumber(startOffset);
		
		int endOffset = startOffset + node.getLength() - 1;
		int endLine = cu.getLineNumber(endOffset);
		int endColumn = cu.getColumnNumber(endOffset);
		
		Range.Position startPosition = new Range.Position(startOffset, startLine, startColumn);
		Range.Position endPosition = new Range.Position(endOffset, endLine, endColumn);
		
		return new Range(Tree.randomId(), startPosition, endPosition);
	}
	
	public static QuickfixType getRewriteQuickFixType(QuickfixRegistry registry) {
		return registry.getQuickfixType(RewriteRefactorings.REWRITE_RECIPE_QUICKFIX);
	}
	
	public static void setRewriteFixes(QuickfixRegistry registry, ReconcileProblemImpl problem, Collection<FixDescriptor> fixDescritptors) {
		QuickfixType quickFixType = getRewriteQuickFixType(registry);
		for (FixDescriptor f : fixDescritptors) {
			problem.addQuickfix(new QuickfixData<>(quickFixType, f, f.getLabel()));
		}
	}
	
	public static String buildLabel(String label, RecipeScope s) {
		switch (s) {
		case FILE:
			return label + " in file";
		case PROJECT:
			return label + " in project";
		default:
			return label;
		}
	}
	
	public static Annotation findAnnotation(BodyDeclaration decl, String annotationFqType, boolean includeMetaHierarchy) {
		for (Iterator<?> itr = decl.modifiers().iterator(); itr.hasNext();) {
			Object mod = itr.next();
			if (mod instanceof Annotation) {
				Annotation a = (Annotation) mod;
				ITypeBinding aType = a.resolveTypeBinding();
				if (aType != null && (
						(includeMetaHierarchy && AnnotationHierarchies.isSubtypeOf(a, annotationFqType)) || (!includeMetaHierarchy && annotationFqType.equals(aType.getQualifiedName()))
				)) {
					return (Annotation) mod;
				}
			}
		}
		return null;
	}

	public static ITypeBinding getDeepErasureType(ITypeBinding type) {
		for (; type != type.getErasure(); type = type.getErasure()) {}
		return type;
	}

}
