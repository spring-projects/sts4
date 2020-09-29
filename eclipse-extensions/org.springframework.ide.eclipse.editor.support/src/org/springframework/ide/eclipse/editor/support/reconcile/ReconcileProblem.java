/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.reconcile;

/**
 * Minamal interface that objects representing a reconciler problem must
 * implement.
 *
 * @author Kris De Volder
 */
public interface ReconcileProblem {
	ProblemType getType();
	String getMessage();
	int getOffset();
	int getLength();
}
