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
package org.springframework.ide.vscode.commons.languageserver;

import org.springframework.ide.vscode.commons.languageserver.util.ShowMessageException;

/**
 * Reports error/warnings from the LS to the client via LSP message
 *
 * @author Alex Boyko
 *
 */
@FunctionalInterface
public interface DiagnosticService {

	/**
	 * Sends the error/warning message to the client
	 * @param message
	 */
	void diagnosticEvent(ShowMessageException message);

}
