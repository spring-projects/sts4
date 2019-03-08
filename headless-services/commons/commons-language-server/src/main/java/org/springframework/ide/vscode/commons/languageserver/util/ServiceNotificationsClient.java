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

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

/**
 * Handler for $ messages. They should/could be ignored since they depend on the
 * implementation capabilities on the server.
 *
 * @author Alex Boyko
 *
 */
@JsonSegment("$")
public interface ServiceNotificationsClient {

	@JsonNotification
	default void setTraceNotification(Object param) {
		// Ignore Message
	}

	@JsonNotification
	default void logTraceNotification(Object param) {
		// Ignore Message
	}

}
