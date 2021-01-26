/*******************************************************************************
 * Copyright (c) 2021 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import org.eclipse.lsp4j.MessageType;

public class ProjectValidation {
	
	public static ProjectValidation OK = new ProjectValidation("", MessageType.Info);

	private final MessageType messageType;
	private final String message;

	public ProjectValidation(String message, MessageType messageType) {
		this.messageType = messageType;
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public MessageType getMessageType() {
		return messageType;
	}
}
