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
package org.springframework.ide.vscode.commons.languageserver.config;

import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

/**
 * Callback to apply configuration to a {@link SimpleLanguageServer}. THis callback is
 * called meant to be called on a newly instantiated SimpleLanguageServer, right after it
 * was created (and prior to actually starting the language server).
 *
 * Deprecated. Uses of this should just be converted on a 'InializingBean' which will make
 * spring framework call them after server bean has been created and all its dependencies
 * injected. This is simpler and gives more flexibility to deal with dependency cycles.
 * For an example See BootLanguageServerIitializer.
 *
 * @author Kris De Volder
 */
@Deprecated
public interface LanguageServerInitializer {
	void initialize(SimpleLanguageServer server) throws Exception;
}
