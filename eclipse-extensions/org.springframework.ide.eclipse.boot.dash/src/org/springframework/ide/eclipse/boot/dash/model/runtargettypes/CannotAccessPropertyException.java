/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

public class CannotAccessPropertyException extends Exception {

	private Exception cause;

	private static final long serialVersionUID = 6564016896619132677L;

	public CannotAccessPropertyException(String message, Exception cause) {
		super(message);
		this.cause = cause;
	}

	public Exception getCause() {
		return cause;
	}

}
