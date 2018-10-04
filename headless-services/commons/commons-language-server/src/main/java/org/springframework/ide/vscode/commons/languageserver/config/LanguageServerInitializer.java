/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.config;

import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

/**
 * Callback to apply configuration to a {@link SimpleLanguageServer}. THis callback is
 * called meant to be called on a newly instantiated SimpleLanguageServer, right after it
 * was created (and prior to actually starting the language server).
 *
 * @author Kris De Volder
 */
public interface LanguageServerInitializer {
	void initialize(SimpleLanguageServer server) throws Exception;
}
