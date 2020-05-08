/*******************************************************************************
 * Copyright (c) 2018, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.composable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.completion.CompositeCompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableMap;

public class CompositeLanguageServerComponents implements LanguageServerComponents {

	private static Logger log = LoggerFactory.getLogger(CompositeLanguageServerComponents.class);

	public static class Builder {
		private Map<LanguageId, LanguageServerComponents> componentsByLanguageId = new HashMap<>();
		private List<ICompletionEngine> completionEngines;

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

		public void completionEngines(List<ICompletionEngine> completionEngines) {
			Assert.isNull("completionEngines should only be set once", this.completionEngines);
			this.completionEngines = completionEngines;
		}
	}

	private final Map<LanguageId, LanguageServerComponents> componentsByLanguageId;
	private final CompositeCompletionEngine completionEngine;
	private final IReconcileEngine reconcileEngine;
	private final HoverHandler hoverHandler;

	public CompositeLanguageServerComponents(SimpleLanguageServer server, Builder builder) {
		this.componentsByLanguageId = ImmutableMap.copyOf(builder.componentsByLanguageId);
		//Create composite Completion engine
		this.completionEngine = new CompositeCompletionEngine();
		for (Entry<LanguageId, LanguageServerComponents> entry : componentsByLanguageId.entrySet()) {
			ICompletionEngine engine = entry.getValue().getCompletionEngine();
			if (engine!=null) {
				completionEngine.add(entry.getKey(), entry.getValue().getCompletionEngine());
			}
		}
		if (builder.completionEngines!=null) {
			for (ICompletionEngine engine : builder.completionEngines) {
				this.completionEngine.add(engine);
			}
		}
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
			public Hover handle(HoverParams params) {
				TextDocument doc = server.getTextDocumentService().get(params.getTextDocument().getUri());
				LanguageId language = doc.getLanguageId();
				LanguageServerComponents subComponents = componentsByLanguageId.get(language);
				if (subComponents!=null) {
					HoverHandler subEngine = subComponents.getHoverProvider();
					if (subEngine != null) {
						return subEngine.handle(params);
					}
				}
				//No applicable subEngine...
				return SimpleTextDocumentService.NO_HOVER;
			}
		};
	}

	@Override
	public Set<LanguageId> getInterestingLanguages() {
		return componentsByLanguageId.keySet();
	}

	@Override
	public ICompletionEngine getCompletionEngine() {
		return completionEngine;
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

}
