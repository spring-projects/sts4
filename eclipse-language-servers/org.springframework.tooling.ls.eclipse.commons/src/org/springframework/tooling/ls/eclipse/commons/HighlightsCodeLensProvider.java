/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.vscode.commons.protocol.HighlightParams;
import org.springframework.tooling.ls.eclipse.commons.preferences.PreferenceConstants;

import com.google.gson.JsonPrimitive;

@SuppressWarnings("restriction")
public class HighlightsCodeLensProvider extends AbstractCodeMiningProvider {

	private static final Map<String, Function<Command, Consumer<MouseEvent>>> ACTION_MAP = new HashMap<>();
	static {
		ACTION_MAP.put("sts.open.url", (cmd) -> {
			return (me) -> {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				if (!cmd.getArguments().isEmpty() && cmd.getArguments().get(0) instanceof JsonPrimitive) {
					String url = ((JsonPrimitive)cmd.getArguments().get(0)).getAsString();
					LSPEclipseUtils.open(url, page, null);
				}
			};
		});
	}

	private static Consumer<MouseEvent> action(Command cmd) {
		if (cmd != null) {
			Function<Command, Consumer<MouseEvent>> func = ACTION_MAP.get(cmd.getCommand());
			if (func != null) {
				return func.apply(cmd);
			}
		}
		return null;
	}

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		IPreferenceStore store = LanguageServerCommonsActivator.getInstance().getPreferenceStore();
		if (store.getBoolean(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS)) {
			IDocument document = viewer.getDocument();
			if (document != null) {
				return CompletableFuture.supplyAsync(() -> {
					List<LSPDocumentInfo> docInfos = LanguageServiceAccessor.getLSPDocumentInfosFor(document, (x) -> true);
					if (!docInfos.isEmpty()) {
						LSPDocumentInfo info = docInfos.get(0);
						HighlightParams highlights = STS4LanguageClientImpl.currentHighlights.get(info.getFileUri().toString());
						if (highlights != null) {
							return highlights.getCodeLenses().stream()
									.filter(codeLens -> codeLens.getCommand() != null)
									.map(codeLens -> {
										try {
											return new HighlightCodeMining(codeLens, document, this, action(codeLens.getCommand()));
										} catch (BadLocationException e) {
											LanguageServerCommonsActivator.logError(e, "Failed to create Eclipse client CodeLens");
											return null;
										}
									})
									.filter(Objects::nonNull)
									.collect(Collectors.toList());
						}
					}
					return Collections.emptyList();
				});
			}
			else {
				return null;
			}
		}
		return null;
	}

	private static class HighlightCodeMining extends LineHeaderCodeMining {

		public HighlightCodeMining(CodeLens codeLens, IDocument document, ICodeMiningProvider provider, Consumer<MouseEvent> action)
				throws BadLocationException {
			super(codeLens.getRange().getStart().getLine(), document, provider, action);
			setLabel(codeLens.getCommand().getTitle());
		}

	}

}
