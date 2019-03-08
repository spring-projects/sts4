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

package org.springframework.ide.vscode.commons.languageserver.util;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public class ShowMessageException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public final MessageParams message;

    public ShowMessageException(MessageParams message, Exception cause) {
        super(message.getMessage(), cause);

        this.message = message;
    }

    public static ShowMessageException error(String message, Exception cause) {
        return create(MessageType.Error, message, cause);
    }

    public static ShowMessageException warning(String message, Exception cause) {
        return create(MessageType.Warning, message, cause);
    }

    private static ShowMessageException create(MessageType warning, String message, Exception cause) {
        MessageParams m = new MessageParams();

        m.setMessage(message);
        m.setType(warning);

        return new ShowMessageException(m, cause);
    }
}
