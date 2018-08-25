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
package org.springframework.tooling.ls.eclipse.commons;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.lsp4j.CodeLens;
import org.springframework.tooling.ls.eclipse.commons.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class HighlightsCodeLensProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		IPreferenceStore store = LanguageServerCommonsActivator.getInstance().getPreferenceStore();
		if (store.getBoolean(PreferenceConstants.HIGHLIGHT_CODELENS_PREFS)) {
			IDocument document = viewer.getDocument();
			List<LSPDocumentInfo> docInfos = LanguageServiceAccessor.getLSPDocumentInfosFor(document, (x) -> true);
			if (!docInfos.isEmpty()) {
				LSPDocumentInfo info = docInfos.get(0);
				HighlightParams highlights = STS4LanguageClientImpl.currentHighlights.get(info.getFileUri().toString());
				if (highlights != null) {
					return CompletableFuture.completedFuture(highlights.getCodeLenses().stream()
							.filter(codeLens -> codeLens.getCommand() != null).map(codeLens -> {
								try {
									return new HighlightCodeMining(codeLens, document, this);
								} catch (BadLocationException e) {
									LanguageServerCommonsActivator.logError(e, "Failed to create Eclipse client CodeLens");
									return null;
								}
							}).filter(Objects::nonNull).collect(Collectors.toList()));
				}
			}
		}
		return null;
	}

	private static class HighlightCodeMining extends LineHeaderCodeMining {

		public HighlightCodeMining(CodeLens codeLens, IDocument document, ICodeMiningProvider provider)
				throws BadLocationException {
			super(codeLens.getRange().getStart().getLine(), document, provider);
			setLabel(codeLens.getCommand().getTitle());
		}

	}

}
