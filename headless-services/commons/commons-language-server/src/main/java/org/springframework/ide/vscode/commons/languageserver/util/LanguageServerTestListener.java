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
package org.springframework.ide.vscode.commons.languageserver.util;

/**
 * A listener used only for testing purposes. It can be attached to a {@link SimpleLanguageServer}
 * to allow tests to receive callbacks for certain 'interesting' points in the language server's
 * processing.
 *
 * @author Kris De Volder
 */
public interface LanguageServerTestListener {
	void reconcileStarted(String uri, int version);
}
