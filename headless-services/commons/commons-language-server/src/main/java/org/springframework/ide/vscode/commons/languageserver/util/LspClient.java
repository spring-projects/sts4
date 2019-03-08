/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

/**
 * LSP Client information utilities
 *
 * @author Alex Boyko
 *
 */
public class LspClient {

	public enum Client {
		UNKNOWN,
		VSCODE,
		ECLIPSE,
		ATOM,
		THEIA,
		INTELLIJ
	}

	/**
	 * Extracts the LSP client from the current environment
	 * @return current LSP client
	 */
	public static Client currentClient() {
		Client client = Client.UNKNOWN;
		String clientStr = System.getProperty("sts.lsp.client");
		if (clientStr != null) {
			try {
				client = Client.valueOf(clientStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				// ignore
			}
		}
		return client;
	}

}
