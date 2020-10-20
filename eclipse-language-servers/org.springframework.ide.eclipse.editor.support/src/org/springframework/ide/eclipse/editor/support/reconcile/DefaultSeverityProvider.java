/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.reconcile;

/**
 * Implementation {@link SeverityProvider} that simplies returns the hard-coded 'default severity' based
 * from the problem's ProblemType.
 *
 * @author Kris De Volder
 */
public class DefaultSeverityProvider implements SeverityProvider {

	@Override
	public ProblemSeverity getSeverity(ProblemType problemType) {
		return problemType.getDefaultSeverity();
	}

	@Override
	public void startSession() {
	}

}
