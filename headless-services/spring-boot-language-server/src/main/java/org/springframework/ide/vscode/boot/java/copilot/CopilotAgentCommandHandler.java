/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.copilot;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.copilot.util.ResponseModifier;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.IndefiniteProgressTask;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

import com.google.gson.JsonElement;

public class CopilotAgentCommandHandler {

	private static final Logger log = LoggerFactory.getLogger(CopilotAgentCommandHandler.class);

	private static final String CMD_COPILOT_AGENT_ENHANCERESPONSE = "sts/copilot/agent/enhanceResponse";

	private static final String CMD_COPILOT_AGENT_LSPEDITS = "sts/copilot/agent/lspEdits";

	private final SimpleLanguageServer server;
	private final JavaProjectFinder projectFinder;
	private final ResponseModifier responseModifier;

	public CopilotAgentCommandHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder,
			ResponseModifier responseModifier) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.responseModifier = responseModifier;
		registerCommands();
	}

	private void registerCommands() {
		server.onCommand(CMD_COPILOT_AGENT_ENHANCERESPONSE, (params) -> {
			return enhanceResponseHandler(params);
		});

		server.onCommand(CMD_COPILOT_AGENT_LSPEDITS, params -> {
			final IndefiniteProgressTask progressTask = server.getProgressService().createIndefiniteProgressTask(UUID.randomUUID().toString(), "Applying GenAI Response", "Computing changes...");
			return CompletableFuture.supplyAsync(() -> {
				try {
					return createLspEdits(params);
				} catch (IOException e) {
					throw new CompletionException(e);
				}
			}).thenCompose(we -> {
				if (progressTask != null) {
					progressTask.progressEvent("Applying document changes...");
				}
				return server.getClient().applyEdit(new ApplyWorkspaceEditParams(we, "Apply GenAI Response")).thenCompose(res -> {
					if (res.isApplied()) {
						return CompletableFuture.completedFuture("success");
					} else {
						return CompletableFuture.completedFuture(null);
					}
				});
			}).whenComplete((o,t) -> progressTask.done());
		});
	}

	private CompletableFuture<Object> enhanceResponseHandler(ExecuteCommandParams params) {
		log.info("Command Handler: ");
		String response = ((JsonElement) params.getArguments().get(0)).getAsString();
		String modifiedResp = responseModifier.modify(response);
		return CompletableFuture.completedFuture(modifiedResp);
	}

	private WorkspaceEdit createLspEdits(ExecuteCommandParams params) throws IOException {
		log.info("Command Handler for lsp edits: ");
		String docURI = ((JsonElement) params.getArguments().get(0)).getAsString();
		String content = ((JsonElement) params.getArguments().get(1)).getAsString();

		IJavaProject project = this.projectFinder.find(new TextDocumentIdentifier(docURI)).get();
		List<ProjectArtifact> projectArtifacts = computeProjectArtifacts(content);
		ProjectArtifactEditGenerator editGenerator = new ProjectArtifactEditGenerator(server.getTextDocumentService(),
				projectArtifacts, Paths.get(project.getLocationUri()), docURI);
		return editGenerator.process().getResult();
	}

	List<ProjectArtifact> computeProjectArtifacts(String response) {
		ProjectArtifactCreator projectArtifactCreator = new ProjectArtifactCreator();
		List<ProjectArtifact> projectArtifacts = projectArtifactCreator.create(response);
		return projectArtifacts;
	}
}