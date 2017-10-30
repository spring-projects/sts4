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
package org.springframework.ide.vscode.boot.java.livehover;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class ASTUtils {

	public static Optional<Range> nameRange(TextDocument doc, Annotation annotation) {
		try {
			int start = annotation.getTypeName().getStartPosition();
			int len = annotation.getTypeName().getLength();
			if (doc.getSafeChar(start - 1) == '@') {
				start--;
				len++;
			}
			return Optional.of(doc.toRange(start, len));
		} catch (Exception e) {
			Log.log(e);
			return Optional.empty();
		}
	}

	public static Optional<String> getValueAttribute(Annotation annotation) {
		if (annotation.isSingleMemberAnnotation()) {
			SingleMemberAnnotation sma = (SingleMemberAnnotation) annotation;
			Expression valueExp = sma.getValue();
			if (valueExp instanceof StringLiteral) {
				StringLiteral stringLit = (StringLiteral) valueExp;
				return Optional.ofNullable(stringLit.getLiteralValue());
			}
		}
		return Optional.empty();
	}

	public static TypeDeclaration findDeclaringType(Annotation annotation) {
		ASTNode node = annotation;
		while (node != null && !(node instanceof TypeDeclaration)) {
			node = node.getParent();
		}

		return node != null ? (TypeDeclaration) node : null;
	}

	public static MethodDeclaration[] findConstructors(TypeDeclaration typeDecl) {
		List<MethodDeclaration> constructors = new ArrayList<>();

		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			if (methodDeclaration.isConstructor()) {
				constructors.add(methodDeclaration);
			}
		}

		return constructors.toArray(new MethodDeclaration[constructors.size()]);
	}
}
