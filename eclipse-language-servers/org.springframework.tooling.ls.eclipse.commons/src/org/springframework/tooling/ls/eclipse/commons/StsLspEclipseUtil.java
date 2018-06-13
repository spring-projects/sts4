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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.LanguageServiceAccessor.LSPDocumentInfo;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class StsLspEclipseUtil {

	public static List<Pair<IDocument, AbstractTextEditor>> getTextEditorsForUri(String uri) {
		List<Pair<IDocument, AbstractTextEditor>> editors = new ArrayList<>(1); //Typical expectation is just one editor.
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editor : page.getEditorReferences()) {
					try {
						IEditorInput input = editor.getEditorInput();
						if (input!=null) {
							IDocument doc = LSPEclipseUtils.getDocument(input);
							Collection<LSPDocumentInfo> infos = LanguageServiceAccessor.getLSPDocumentInfosFor(doc, (x) -> true);
							for (LSPDocumentInfo lspDoc : infos) {
								if (uri.equals(lspDoc.getFileUri().toString())) {
									boolean restore = false;
									IEditorPart editorPart = editor.getEditor(restore );
									if (editorPart instanceof AbstractTextEditor) {
										editors.add(Pair.of(doc, (AbstractTextEditor) editorPart));
									}
								}
							}
						}
					} catch (PartInitException e) {
						LanguageServerCommonsActivator.logError(e, e.getMessage());
					}
				}
			}
		}
		return editors;
	}

}
