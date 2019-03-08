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
package org.springframework.ide.vscode.commons.languageserver.quickfix;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Assert;

import reactor.core.publisher.Mono;

/**
 * Registry keeping track of quickfix types for a {@link SimpleLanguageServer}.
 * <p>
 * Each type must be associated with a handler. The handler accepts a parameter object
 * and computes workspace edits to be applied when the quickfix is executed.
 * <p>
 * The parameters object must be convertible to json since it is sent over the
 * wire to the client and later sent back when the quickfix is selected.
 *
 * @author Kris De Volder
 */
public class QuickfixRegistry {

	private Map<String, QuickfixHandler> registry = new HashMap<>();

	public synchronized QuickfixType register(String typeName, QuickfixHandler handler) {
		Assert.isLegal(!registry.containsKey(typeName), "Quickfix type already registered: '"+typeName);
		registry.put(typeName, handler);
		return new QuickfixType() {

			@Override
			public QuickfixEdit createEdits(Object params) {
				return handler.createEdits(params);
			}

			@Override
			public String getId() {
				return typeName;
			}
		};
	}

	public Mono<QuickfixEdit> handle(QuickfixResolveParams params) {
		QuickfixHandler handler = registry.get(params.getType());
		return Mono.fromSupplier(() -> {
			return handler.createEdits(params.getParams());
		});
	}

	public boolean hasFixes() {
		return !registry.isEmpty();
	}
}
