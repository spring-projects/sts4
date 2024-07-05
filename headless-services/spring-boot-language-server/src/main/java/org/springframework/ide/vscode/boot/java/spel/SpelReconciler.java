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
package org.springframework.ide.vscode.boot.java.spel;

import org.springframework.ide.vscode.boot.java.SpelProblemType;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.AntlrReconciler;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.parser.spel.SpelLexer;
import org.springframework.ide.vscode.parser.spel.SpelParser;

public class SpelReconciler extends AntlrReconciler {
	
	private boolean enabled;
		
	public SpelReconciler() {
		super("SPEL", SpelParser.class, SpelLexer.class, "spelExpr", SpelProblemType.JAVA_SPEL_EXPRESSION_SYNTAX);
		this.enabled = true;
	}

	public void setEnabled(boolean spelExpressionValidationEnabled) {
		this.enabled = spelExpressionValidationEnabled;
	}

	@Override
	public void reconcile(String text, int startPosition, IProblemCollector problemCollector) {
		if (!enabled) {
			return;
		}
		super.reconcile(text, startPosition, problemCollector);
	}

}
