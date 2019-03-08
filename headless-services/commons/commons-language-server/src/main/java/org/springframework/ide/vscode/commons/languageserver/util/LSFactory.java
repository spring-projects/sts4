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
 * A factory that is aware of the language server context. Use this to inject
 * dependencies into a {@link SimpleLanguageServer} implementation when the
 * instantiation of the injected component itself depends on the language server (or
 * some of its services).
 *
 * Note: It is starting to 'smell' like we should really be using some kind of dependency
 * injection framework to deal with stuff like this.
 */
@FunctionalInterface
public interface LSFactory<T> {
	T create(SimpleLanguageServer server);
}
