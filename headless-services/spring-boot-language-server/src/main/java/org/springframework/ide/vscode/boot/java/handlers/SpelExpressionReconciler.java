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

import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemTypes;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.util.SystemPropertyUtils;

/**
 * @author Martin Lippert
 */
public class SpelExpressionReconciler implements Reconciler {

	private boolean spelExpressionValidationEnabled;
	
	public SpelExpressionReconciler() {
		this.spelExpressionValidationEnabled = true;
	}

	public void setEnabled(boolean spelExpressionValidationEnabled) {
		this.spelExpressionValidationEnabled = spelExpressionValidationEnabled;
	}
	
	@Override
	public void reconcile(String spelExpression, int startPosition, IProblemCollector problemCollector) {
		if (!this.spelExpressionValidationEnabled) {
			return;
		}
		
		if (spelExpression.length() > 0 && spelExpression.indexOf(SystemPropertyUtils.PLACEHOLDER_PREFIX) == -1) {
			SpelExpressionParser parser = new SpelExpressionParser();
			try {
				parser.parseExpression(spelExpression);
			}
			catch (ParseException e) {
				String message = e.getSimpleMessage();
				int position = e.getPosition();
				
				createProblem(spelExpression, message, startPosition, position, problemCollector);
			}
		}
	}

	private void createProblem(String spelExpression, String message, int startPosition, int position, IProblemCollector problemCollector) {
		int start = startPosition + position;
		int length = spelExpression.length() - position;
		
		ReconcileProblem problem = new ReconcileProblemImpl(ProblemTypes.create("SpEL Expression Problem", ProblemSeverity.ERROR), message, start, length);
		problemCollector.accept(problem);
	}

}
