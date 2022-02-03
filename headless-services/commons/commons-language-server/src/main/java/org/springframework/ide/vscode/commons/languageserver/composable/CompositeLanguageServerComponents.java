/*******************************************************************************
 * Copyright (c) 2018, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.composable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.CodeActionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableMap;

public class CompositeLanguageServerComponents implements LanguageServerComponents {

	public static class Builder {
		private Map<LanguageId, LanguageServerComponents> componentsByLanguageId = new HashMap<>();

		public void add(LanguageServerComponents components) {
			for (LanguageId language : components.getInterestingLanguages()) {
				//Multiple associations to a single language id not yet supported.
				Assert.isLegal(!componentsByLanguageId.containsKey(language));
				componentsByLanguageId.put(language, components);
			}
		}

		public CompositeLanguageServerComponents build(SimpleLanguageServer server) {
			return new CompositeLanguageServerComponents(server, this);
		}
	}

	private final Map<LanguageId, LanguageServerComponents> componentsByLanguageId;
	private final IReconcileEngine reconcileEngine;
	private final HoverHandler hoverHandler;
	private final CodeActionHandler codeActionHandler;

	public CompositeLanguageServerComponents(SimpleLanguageServer server, Builder builder) {
		this.componentsByLanguageId = ImmutableMap.copyOf(builder.componentsByLanguageId);
		//Create composite Reconcile engine
		if (componentsByLanguageId.values().stream().map(LanguageServerComponents::getReconcileEngine).anyMatch(Optional::isPresent)) {
			this.reconcileEngine = new IReconcileEngine() {
				@Override
				public void reconcile(IDocument document, IProblemCollector problemCollector) {
					LanguageId language = document.getLanguageId();
					LanguageServerComponents subComponents = componentsByLanguageId.get(language);
					if (subComponents!=null) {
						Optional<IReconcileEngine> subEngine = subComponents.getReconcileEngine();
						if (subEngine.isPresent()) {
							subEngine.get().reconcile(document, problemCollector);
							return;
						}
					}
					//No applicable subEngine... but we still have to obey the IReconcileEngine contract!
					IReconcileEngine.NULL.reconcile(document, problemCollector);
				}
			};
		} else {
			this.reconcileEngine = null;
		}
		//Create composite hover handler
		this.hoverHandler = new HoverHandler() {
			@Override
			public Hover handle(CancelChecker cancelToken, HoverParams params) {
				TextDocument doc = server.getTextDocumentService().getLatestSnapshot(params.getTextDocument().getUri());
				LanguageId language = doc.getLanguageId();
				LanguageServerComponents subComponents = componentsByLanguageId.get(language);
				if (subComponents != null) {
					HoverHandler subEngine = subComponents.getHoverProvider();
					if (subEngine != null) {
						return subEngine.handle(cancelToken, params);
					}
				}
				//No applicable subEngine...
				return SimpleTextDocumentService.NO_HOVER;
			}
		};
		
		this.codeActionHandler = new CodeActionHandler() {
			
			@Override
			public List<Either<Command, CodeAction>> handle(CancelChecker cancelToken, CodeActionCapabilities capabilities, TextDocument doc,
					IRegion region) {
				LanguageId language = doc.getLanguageId();
				LanguageServerComponents subComponents = componentsByLanguageId.get(language);
				if (subComponents != null) {
					return subComponents.getCodeActionProvider()
							.map(subEngine -> subEngine.handle(cancelToken, capabilities, doc, region))
							.orElse(Collections.emptyList());
				}
				//No applicable subEngine...
				return Collections.emptyList();
			}
		};
	}

	@Override
	public Set<LanguageId> getInterestingLanguages() {
		return componentsByLanguageId.keySet();
	}

	@Override
	public HoverHandler getHoverProvider() {
		return hoverHandler;
	}

	@Override
	public Optional<IReconcileEngine> getReconcileEngine() {
		return Optional.ofNullable(reconcileEngine);
	}

	@SuppressWarnings("unchecked")
	public <C extends LanguageServerComponents> C get(Class<C> subComponentsType) {
		for (LanguageServerComponents subcomp : this.componentsByLanguageId.values()) {
			if (subComponentsType.isInstance(subcomp)) {
				return (C) subcomp;
			}
		}
		return null;
	}

	@Override
	public Optional<CodeActionHandler> getCodeActionProvider() {
		return Optional.of(codeActionHandler);
	}

}
