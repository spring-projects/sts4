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
package org.springframework.ide.vscode.boot.java.copilot.util;

@SuppressWarnings("serial")
public class SpringCliException extends RuntimeException {

	/**
	 * Instantiates a new {@code UpException}.
	 * @param message the message
	 */
	public SpringCliException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new {@code UpException}.
	 * @param message the message
	 * @param cause the cause
	 */
	public SpringCliException(String message, Throwable cause) {
		super(message, cause);
	}

}