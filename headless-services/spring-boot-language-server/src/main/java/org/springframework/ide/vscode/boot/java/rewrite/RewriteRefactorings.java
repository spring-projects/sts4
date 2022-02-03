/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.springframework.ide.vscode.commons.languageserver.util.CodeActionResolver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RewriteRefactorings implements CodeActionResolver {
	
	private Map<String, Function<List<?>, WorkspaceEdit>> refactoringsMap = new ConcurrentHashMap<>();
	
	public void addRefactoring(String id, Function<List<?>, WorkspaceEdit> handler) {
		if (refactoringsMap.containsKey(id)) {
			throw new IllegalStateException("Refactoring with id '" + id + "' already exists!");
		}
		refactoringsMap.put(id, handler);
	}
	
	@Override
	public void resolve(CodeAction codeAction) {
		if (codeAction.getData() instanceof JsonObject) {
			JsonObject o = (JsonObject) codeAction.getData();
			try {
				Data data = new Gson().fromJson(o, Data.class);
				if (data != null && data.id != null) {
					Function<List<?>, WorkspaceEdit> handler = refactoringsMap.get(data.id);
					if (handler != null) {
						WorkspaceEdit edit = handler.apply(data.arguments);
						if (edit != null) {
							codeAction.setEdit(edit);
						}
					}
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public static class Data {
		public String id;
		public List<?> arguments;
		public Data(String id, List<?> arguments) {
			this.id = id;
			this.arguments = arguments;
		}
	}

}
