/*******************************************************************************
 * Copyright (c) 2018, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.composable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.CodeActionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.CodeLensHandler;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.InlayHintHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableMap;

public class CompositeLanguageServerComponents implements LanguageServerComponents {

	public static class Builder {
		private Map<LanguageId, List<LanguageServerComponents>> componentsByLanguageId = new HashMap<>();

		public void add(LanguageServerComponents components) {
			for (LanguageId language : components.getInterestingLanguages()) {
				List<LanguageServerComponents> list = componentsByLanguageId.get(language);
				if (list == null) {
					list = new ArrayList<>(1);
					componentsByLanguageId.put(language, list);
				}
				list.add(components);
			}
		}

		public CompositeLanguageServerComponents build(SimpleLanguageServer server) {
			return new CompositeLanguageServerComponents(server, this);
		}
	}

	private final Map<LanguageId, List<LanguageServerComponents>> componentsByLanguageId;
	private final IReconcileEngine reconcileEngine;
	private final HoverHandler hoverHandler;
	private final CodeActionHandler codeActionHandler;
	private final CodeLensHandler codeLensHandler;
	private final DocumentSymbolHandler docSymbolHandler;
	private final InlayHintHandler inlayHintHandler;

	public CompositeLanguageServerComponents(SimpleLanguageServer server, Builder builder) {
		this.componentsByLanguageId = ImmutableMap.copyOf(builder.componentsByLanguageId);
		//Create composite Reconcile engine
		if (componentsByLanguageId.values().stream().flatMap(l -> l.stream()).map(LanguageServerComponents::getReconcileEngine).anyMatch(Optional::isPresent)) {
			this.reconcileEngine = new IReconcileEngine() {
				@Override
				public void reconcile(IDocument document, IProblemCollector problemCollector) {
					LanguageId language = document.getLanguageId();
					List<LanguageServerComponents> subComponents = componentsByLanguageId.get(language);
					if (subComponents!=null) {
						for (LanguageServerComponents subComponent : subComponents) {
							Optional<IReconcileEngine> subEngine = subComponent.getReconcileEngine();
							if (subEngine.isPresent()) {
								subEngine.get().reconcile(document, problemCollector);
							}
						}
					}
//					//No applicable subEngine... but we still have to obey the IReconcileEngine contract!
//					IReconcileEngine.NULL.reconcile(document, problemCollector);
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
				List<LanguageServerComponents> subComponents = componentsByLanguageId.get(language);
				if (subComponents != null) {
					for (LanguageServerComponents subComponent : subComponents) {
						HoverHandler subEngine = subComponent.getHoverProvider();
						if (subEngine != null) {
							return subEngine.handle(cancelToken, params);
						}
					}
				}
				//No applicable subEngine...
				return SimpleTextDocumentService.NO_HOVER;
			}
		};
		
		this.codeActionHandler = new CodeActionHandler() {
			
			@Override
			public List<Either<Command, CodeAction>> handle(CancelChecker cancelToken,
					CodeActionCapabilities capabilities, CodeActionContext context, TextDocument doc, IRegion region) {
				LanguageId language = doc.getLanguageId();
				List<LanguageServerComponents> subComponents = componentsByLanguageId.get(language);
				if (subComponents != null) {
					return subComponents.stream()
						.map(sc -> sc.getCodeActionProvider())
						.filter(provider -> provider.isPresent())
						.flatMap(se -> se.get().handle(cancelToken, capabilities, context, doc, region).stream())
						.collect(Collectors.toList());
				}
				// No applicable subEngine...
				return Collections.emptyList();
			}
		};
		
		this.codeLensHandler = new CodeLensHandler() {
			
			@Override
			public List<? extends CodeLens> handle(CancelChecker cancelToken, CodeLensParams params) {
				TextDocument doc = server.getTextDocumentService().getLatestSnapshot(params.getTextDocument().getUri());
				LanguageId language = doc.getLanguageId();
				List<LanguageServerComponents> subComponents = componentsByLanguageId.get(language);
				if (subComponents != null) {
					return subComponents.stream()
							.map(sc -> sc.getCodeLensHandler())
							.filter(h -> h.isPresent())
							.flatMap(h -> h.get().handle(cancelToken, params).stream())
							.collect(Collectors.toList());
				}
				// No applicable subEngine...
				return Collections.emptyList();
			}
		};
		
		this.inlayHintHandler = new InlayHintHandler() {
			
			@Override
			public List<InlayHint> handle(CancelChecker token, InlayHintParams params) {
				TextDocument doc = server.getTextDocumentService().getLatestSnapshot(params.getTextDocument().getUri());
				LanguageId language = doc.getLanguageId();
				List<LanguageServerComponents> subComponents = componentsByLanguageId.get(language);
				if (subComponents != null) {
					return subComponents.stream()
							.map(sc -> sc.getInlayHintHandler())
							.filter(h -> h.isPresent())
							.flatMap(h -> h.get().handle(token, params).stream())
							.collect(Collectors.toList());
				}
				// No applicable subEngine...
				return Collections.emptyList();
			}
		};
		
		this.docSymbolHandler = new DocumentSymbolHandler() {
			
			@Override
			public List<? extends WorkspaceSymbol> handle(DocumentSymbolParams params) {
				TextDocument doc = server.getTextDocumentService().getLatestSnapshot(params.getTextDocument().getUri());
				if (doc != null) {
					LanguageId language = doc.getLanguageId();
					List<LanguageServerComponents> subComponents = componentsByLanguageId.get(language);
					if (subComponents != null) {
						return subComponents.stream()
								.map(sc -> sc.getDocumentSymbolProvider())
								.filter(ds -> ds.isPresent())
								.flatMap(ds -> ds.get().handle(params).stream())
								.collect(Collectors.toList());
					}
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

	@Override
	public Optional<DocumentSymbolHandler> getDocumentSymbolProvider() {
		return Optional.ofNullable(docSymbolHandler);
	}

	public <C extends LanguageServerComponents> C get(Class<C> subComponentsType) {
		return this.componentsByLanguageId.values().stream()
				.flatMap(l -> l.stream())
				.filter(subComponentsType::isInstance)
				.map(subComponentsType::cast)
				.findFirst()
				.orElse(null);
	}

	@Override
	public Optional<CodeActionHandler> getCodeActionProvider() {
		return Optional.of(codeActionHandler);
	}

	@Override
	public Optional<CodeLensHandler> getCodeLensHandler() {
		return Optional.of(codeLensHandler);
	}

	@Override
	public Optional<InlayHintHandler> getInlayHintHandler() {
		return Optional.of(inlayHintHandler);
	}

}
