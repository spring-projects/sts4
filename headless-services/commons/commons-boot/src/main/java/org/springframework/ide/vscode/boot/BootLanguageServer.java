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
package org.springframework.ide.vscode.boot;

import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.properties.BootPropertiesLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.composable.ComposableLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.composable.LanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

public class BootLanguageServer<C extends LanguageServerComponents> extends ComposableLanguageServer<C> {

	private BootLanguageServer(String extensionId, LSFactory<C> _components) {
		super(extensionId, _components);
	}

	public static ComposableLanguageServer<BootPropertiesLanguageServerComponents> create(LSFactory<BootLanguageServerParams> params) {
		//TODO compose properties and java
		return new ComposableLanguageServer<>("vscode-boot", s -> new BootPropertiesLanguageServerComponents(s, params));
	}
	
	public static ComposableLanguageServer<BootPropertiesLanguageServerComponents> createProperties(LSFactory<BootLanguageServerParams> params) {
		return new ComposableLanguageServer<>("vscode-boot-properties", s -> new BootPropertiesLanguageServerComponents(s, params));
	}

	public static ComposableLanguageServer<BootJavaLanguageServerComponents> createJava(LSFactory<BootLanguageServerParams> params) {
		return new ComposableLanguageServer<>("vscode-boot-java", s -> new BootJavaLanguageServerComponents(s, params));
	}

}
