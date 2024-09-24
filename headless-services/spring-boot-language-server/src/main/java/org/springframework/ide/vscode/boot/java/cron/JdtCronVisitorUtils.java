/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.cron;

import static org.springframework.ide.vscode.boot.java.data.jpa.queries.JdtQueryVisitorUtils.extractEmbeddedExpression;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.JdtQueryVisitorUtils.EmbeddedExpression;

public class JdtCronVisitorUtils {
	
	static final String SCHEDULED_SIMPLE_NAME = "Scheduled";
	
	public static EmbeddedExpression extractCron(NormalAnnotation node) {
		if (node.getTypeName() != null) {
			String fqn = node.getTypeName().getFullyQualifiedName();
			if (SCHEDULED_SIMPLE_NAME.equals(fqn) || Annotations.SCHEDULED.equals(fqn)) {
				ITypeBinding typeBinding = node.resolveTypeBinding();
				if (typeBinding != null && Annotations.SCHEDULED.equals(typeBinding.getQualifiedName())) {
					for (Object value : node.values()) {
						if (value instanceof MemberValuePair) {
							MemberValuePair pair = (MemberValuePair) value;
							String name = pair.getName().getFullyQualifiedName();
							if (name != null && "cron".equals(name) && isCronExpression(pair.getValue())) {
								return extractEmbeddedExpression(pair.getValue());
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean isCronExpression(Expression e) {
		String value = null;
		if (e instanceof StringLiteral sl) {
			value = sl.getLiteralValue();
		} else if (e instanceof TextBlock tb) {
			value = tb.getLiteralValue();
		}
		value = value.trim();
		if (value.startsWith("#{") || value.startsWith("${")) {
			// Either SPEL or Property Holder
			return false;
		}
		return value != null;
	}


}
