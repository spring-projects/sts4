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
package org.springframework.ide.vscode.boot.properties;

import org.springframework.ide.vscode.commons.languageserver.composable.ComposableLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.composable.LanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;

public class BootLanguageServer<C extends LanguageServerComponents> extends ComposableLanguageServer<C> {

	private BootLanguageServer(String extensionId, LSFactory<C> _components) {
		super(extensionId, _components);
	}

	public static ComposableLanguageServer<BootPropertiesLanguageServerComponents> create(LSFactory<BootPropertiesLanguageServerParams> params) {
		return new ComposableLanguageServer<>("vscode-boot", s -> new BootPropertiesLanguageServerComponents(s, params));
	}
	
	public static ComposableLanguageServer<BootPropertiesLanguageServerComponents> createProperties(LSFactory<BootPropertiesLanguageServerParams> params) {
		return new ComposableLanguageServer<>("vscode-boot", s -> new BootPropertiesLanguageServerComponents(s, params));
	}

}
