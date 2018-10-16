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
package org.springframework.ide.vscode.boot.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.properties.BootPropertiesLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.composable.ComposableLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.composable.CompositeLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class BootLanguageServerInitializer implements InitializingBean {

	@Autowired SimpleLanguageServer server;

	@Autowired BootLanguageServerParams params;

	private CompositeLanguageServerComponents components;

	private ComposableLanguageServer<CompositeLanguageServerComponents> composableLs;

	private static final Logger log = LoggerFactory.getLogger(BootLanguageServerInitializer.class);

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

	@Override
	public void afterPropertiesSet() throws Exception {
		//TODO: ComposableLanguageServer object instance serves no purpose anymore. The constructor really just contains
		// some server intialization code. Migrate that code and get rid of the ComposableLanguageServer class
		CompositeLanguageServerComponents.Builder builder = new CompositeLanguageServerComponents.Builder();
		builder.add(new BootPropertiesLanguageServerComponents(server, (ignore) -> params));
		builder.add(new BootJavaLanguageServerComponents(server, (ignore) -> params));
		components = builder.build(server);
		params.projectObserver.addListener(reconcileOpenDocuments(server, components));
		this.composableLs = new ComposableLanguageServer<>(server, components);
	}

	public CompositeLanguageServerComponents getComponents() {
		Assert.notNull(components, "Not yet initialized, can't get components yet.");
		return components;
	}

	public void setMaxCompletions(int maxCompletions) {
		composableLs.setMaxCompletionsNumber(maxCompletions);
	}

}
