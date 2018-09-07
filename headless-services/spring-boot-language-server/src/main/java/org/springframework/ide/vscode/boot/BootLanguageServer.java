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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.properties.BootPropertiesLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.composable.ComposableLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.composable.CompositeLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.composable.LanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class BootLanguageServer<C extends LanguageServerComponents> extends ComposableLanguageServer<C> {

	private static final Logger log = LoggerFactory.getLogger(BootLanguageServer.class);

	private static ProjectObserver.Listener reconcileOpenDocuments(SimpleLanguageServer s, CompositeLanguageServerComponents c) {
		return ProjectObserver.onAny(project -> {
			c.getReconcileEngine().ifPresent(reconciler -> {
				log.info("A project changed {}, triggering reconcile on all open documents", project.getElementName());
				for (TextDocument doc : s.getTextDocumentService().getAll()) {
					s.validateWith(doc.getId(), reconciler);
				}
			});
		});
	}

	private BootLanguageServer(String extensionId, LSFactory<C> _components) {
		super(extensionId, _components);
	}

	public static ComposableLanguageServer<CompositeLanguageServerComponents> create(LSFactory<BootLanguageServerParams> _params) {
		return new ComposableLanguageServer<>("vscode-boot", s -> {
			BootLanguageServerParams params = _params.create(s);
			CompositeLanguageServerComponents.Builder builder = new CompositeLanguageServerComponents.Builder();
			builder.add(new BootPropertiesLanguageServerComponents(s, (ignore) -> params));
			builder.add(new BootJavaLanguageServerComponents(s, (ignore) -> params));
			CompositeLanguageServerComponents components = builder.build(s);
			params.projectObserver.addListener(reconcileOpenDocuments(s, components));
			return components;
		});
	}

	public static ComposableLanguageServer<BootPropertiesLanguageServerComponents> createProperties(LSFactory<BootLanguageServerParams> params) {
		return new ComposableLanguageServer<>("vscode-boot-properties", s -> new BootPropertiesLanguageServerComponents(s, params));
	}

	public static ComposableLanguageServer<BootJavaLanguageServerComponents> createJava(LSFactory<BootLanguageServerParams> params) {
		return new ComposableLanguageServer<>("vscode-boot-java", s -> new BootJavaLanguageServerComponents(s, params));
	}

}
