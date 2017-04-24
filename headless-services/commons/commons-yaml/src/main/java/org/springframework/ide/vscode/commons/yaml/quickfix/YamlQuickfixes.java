/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.quickfix;

import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits.TextReplace;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixType;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.completion.YamlPathEdits;
import org.springframework.ide.vscode.commons.yaml.path.YamlPath;
import org.springframework.ide.vscode.commons.yaml.path.YamlPathSegment;
import org.springframework.ide.vscode.commons.yaml.reconcile.MissingPropertiesData;
import org.springframework.ide.vscode.commons.yaml.reconcile.ReplaceStringData;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SChildBearingNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class YamlQuickfixes {

	public final QuickfixType MISSING_PROP_FIX;
	public final QuickfixType SIMPLE_TEXT_EDIT;

	public YamlQuickfixes(QuickfixRegistry r, SimpleTextDocumentService textDocumentService, YamlStructureProvider structureProvider) {
		MISSING_PROP_FIX = r.register("MISSING_PROP_FIX", (Object _params) -> {
			MissingPropertiesData params = new ObjectMapper().convertValue(_params, MissingPropertiesData.class);
			try {
				TextDocument _doc = textDocumentService.getDocument(params.getUri());
				if (_doc!=null) {
					YamlDocument doc = new YamlDocument(_doc, structureProvider);
					SNode root = doc.getStructure();
					if (root!=null) {
						YamlPath path = YamlPath.decode(params.getPath());
						SNode _target = path.traverse(root);
						if (_target instanceof SChildBearingNode) {
							YamlPathEdits edits = new YamlPathEdits(doc);
							SChildBearingNode target = (SChildBearingNode) _target;
							for (String prop : params.getProps()) {
								edits.createPath(target, new YamlPath(YamlPathSegment.valueAt(prop)), " ");
							}
							TextReplace replaceEdit = edits.asReplacement(_doc);
							if (replaceEdit!=null) {
								WorkspaceEdit wsEdits = new WorkspaceEdit();
								wsEdits.setChanges(ImmutableMap.of(
										params.getUri(),
										ImmutableList.of(new TextEdit(_doc.toRange(replaceEdit.getRegion()), replaceEdit.newText))
								));
								return wsEdits;
							}
						}
					}
				}
			} catch (Exception e) {
				Log.log(e);
			}
			//Something went wrong. Return empty edit object.
			return new WorkspaceEdit(ImmutableMap.of(), null);
		});

		SIMPLE_TEXT_EDIT = r.register("SIMPLE_TEXT_EDIT", (_params) -> {
			try {
				ReplaceStringData params = new ObjectMapper().convertValue(_params, ReplaceStringData.class);
				TextDocument _doc = textDocumentService.getDocument(params.getUri());
				if (_doc!=null) {
					return new WorkspaceEdit(
							ImmutableMap.of(params.getUri(), ImmutableList.of(params.getEdit())),
							null
					);
				}
			} catch (Exception e) {
				Log.log(e);
			}
			//Something went wrong. Return empty edit object.
			//Something went wrong. Return empty edit object.
			return new WorkspaceEdit(ImmutableMap.of(), null);
		});
	}

}
