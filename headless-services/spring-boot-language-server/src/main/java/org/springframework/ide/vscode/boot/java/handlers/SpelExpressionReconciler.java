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

import java.util.function.Function;

import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.ide.vscode.boot.java.SpelProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.Region;
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
	public void reconcile(String spelExpression, Function<IRegion, IRegion> mapper, IProblemCollector problemCollector) {
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
				IRegion r = mapper.apply(new Region(0, position)); 
				ReconcileProblem problem = new ReconcileProblemImpl(SpelProblemType.JAVA_SPEL_EXPRESSION_SYNTAX, message, r.getOffset(), r.getLength());
				problemCollector.accept(problem);
			}
		}
	}

}
