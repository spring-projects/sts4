/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
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
 * An info object that can be attached to a {@link ReconcileException} to indicate that
 * the problem can be fixed by a simple value substitution at the exact location of
 * the problem marker.
 *
 * @author Kris De Volder
 */
public class ReplacementQuickfix {
	public final String msg;
	public final String replacement;
	public ReplacementQuickfix(String msg, String replacement) {
		super();
		this.msg = msg;
		this.replacement = replacement;
	}
	@Override
	public String toString() {
		return "ReplacementQuickfix [msg=" + msg + ", replacement=" + replacement + "]";
	}
}