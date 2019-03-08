/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver.reconcile;

/**
 * Besides the methods below, the only hard requirement for a 'problem type' is
 * that it is a unique object that is not 'equals' to any other object.
 * <p>
 * It is probably nice if you implement a good toString however.
 * <p>
 * A good way to implement a discrete set of problemType objects is as an enum
 * that implements this interace.
 *
 * @author Kris De Volder
 */
public interface ProblemType {
	ProblemSeverity getDefaultSeverity();
	String toString();
	String getCode();
}
