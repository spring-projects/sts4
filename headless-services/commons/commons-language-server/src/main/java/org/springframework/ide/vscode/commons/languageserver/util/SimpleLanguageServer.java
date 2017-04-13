/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.vscode.commons.languageserver.ProgressParams;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.languageserver.STS4LanguageClient;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix.QuickfixData;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixResolveParams;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.CollectionUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Abstract base class to implement LanguageServer. Bits and pieces copied from
 * the 'JavaLanguageServer' example which seem generally useful / reusable end up in
 * here so we can try to keep the subclass itself more 'clutter free' and focus on
 * what its really doing and not the 'wiring and plumbing'.
 */
public abstract class SimpleLanguageServer implements LanguageServer, LanguageClientAware, ServiceNotificationsClient {

    private static final Logger LOG = Logger.getLogger(SimpleLanguageServer.class.getName());

	private static final Scheduler RECONCILER_SCHEDULER = Schedulers.newSingle("Reconciler");

	public final String EXTENSION_ID;

    private Path workspaceRoot;

	private SimpleTextDocumentService tds;

	private SimpleWorkspaceService workspace;

	private STS4LanguageClient client;

	private ProgressService progressService = (String taskId, String statusMsg) -> {
		STS4LanguageClient client = SimpleLanguageServer.this.client;
		if (client!=null) {
			client.progress(new ProgressParams(taskId, statusMsg));
		}
	};

	private CompletableFuture<Void> busyReconcile = CompletableFuture.completedFuture(null);

	private QuickfixRegistry quickfixRegistry;

	@Override
	public void connect(LanguageClient _client) {
		this.client = (STS4LanguageClient) _client;
	}

	protected synchronized QuickfixRegistry getQuickfixRegistry() {
		if (quickfixRegistry==null) {
			quickfixRegistry = new QuickfixRegistry();
		}
		return quickfixRegistry;
	}

	public SimpleLanguageServer(String extensionId) {
		this.EXTENSION_ID = extensionId;
	}

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
//    	LOG.info("Initializing");
    	String rootPath = params.getRootPath();
    	if (rootPath==null) {
//	        LOG.warning("workspaceRoot NOT SET");
    	} else {
	        this.workspaceRoot= Paths.get(rootPath).toAbsolutePath().normalize();
//	        LOG.info("workspaceRoot = "+workspaceRoot);
    	}

        InitializeResult result = new InitializeResult();

        ServerCapabilities cap = getServerCapabilities();
        result.setCapabilities(cap);

        return CompletableFuture.completedFuture(result);
    }

	public void onError(String message, Throwable error) {
		LanguageClient cl = this.client;
		if (cl != null) {
			if (error instanceof ShowMessageException)
				client.showMessage(((ShowMessageException) error).message);
			else {
				LOG.log(Level.SEVERE, message, error);

				MessageParams m = new MessageParams();

				m.setMessage(message);
				m.setType(MessageType.Error);
				client.showMessage(m);
			}
		}
	}

	protected final ServerCapabilities getServerCapabilities() {
		ServerCapabilities c = new ServerCapabilities();

		c.setTextDocumentSync(TextDocumentSyncKind.Incremental);
		c.setHoverProvider(true);

		CompletionOptions completionProvider = new CompletionOptions();
		completionProvider.setResolveProvider(false);
		c.setCompletionProvider(completionProvider);

		if (hasQuickFixes()) {
			c.setCodeActionProvider(true);
		}
		if (hasDefinitionHandler()) {
			c.setDefinitionProvider(true);
		}
		if (hasReferencesHandler()) {
			c.setReferencesProvider(true);
		}
		if (hasDocumentSymbolHandler()) {
			c.setDocumentSymbolProvider(true);
		}

		return c;
	}

	private boolean hasDocumentSymbolHandler() {
		return getTextDocumentService().hasDocumentSymbolHandler();
	}

	private boolean hasReferencesHandler() {
		return getTextDocumentService().hasReferencesHandler();
	}

	private boolean hasDefinitionHandler() {
		return getTextDocumentService().hasDefinitionHandler();
	}

	private boolean hasQuickFixes() {
		return quickfixRegistry!=null && quickfixRegistry.hasFixes();
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return CompletableFuture.completedFuture(new Object());
	}

	@Override
	public void exit() {
		System.exit(0);
	}

	public Path getWorkspaceRoot() {
		return workspaceRoot;
	}

	@Override
	public synchronized SimpleTextDocumentService getTextDocumentService() {
		if (tds==null) {
			tds = createTextDocumentService();
		}
		return tds;
	}

	protected SimpleTextDocumentService createTextDocumentService() {
		return new SimpleTextDocumentService(this);
	}

	public SimpleWorkspaceService createWorkspaceService() {
		return new SimpleWorkspaceService();
	}

	@Override
	public synchronized SimpleWorkspaceService getWorkspaceService() {
		if (workspace==null) {
			workspace = createWorkspaceService();
		}
		return workspace;
	}

	/**
	 * Convenience method. Subclasses can call this to use a {@link IReconcileEngine} ported
	 * from old STS codebase to validate a given {@link TextDocument} and publish Diagnostics.
	 */
	protected void validateWith(TextDocument _doc, IReconcileEngine engine) {
		TextDocument doc = _doc.copy();

		CompletableFuture<Void> reconcileSession = this.busyReconcile = new CompletableFuture<Void>();

		SimpleTextDocumentService documents = getTextDocumentService();
		IProblemCollector problems = new IProblemCollector() {

			private List<Diagnostic> diagnostics = new ArrayList<>();
			private List<Quickfix> quickfixes = new ArrayList<>();

			@Override
			public void endCollecting() {
				documents.setQuickfixes(doc, quickfixes);
				documents.publishDiagnostics(doc, diagnostics);
			}

			@Override
			public void beginCollecting() {
				diagnostics.clear();
			}

			@Override
			public void accept(ReconcileProblem problem) {
				try {
					DiagnosticSeverity severity = getDiagnosticSeverity(problem);
					if (severity!=null) {
						Diagnostic d = new Diagnostic();
						d.setCode(problem.getCode());
						d.setMessage(problem.getMessage());
						Range rng = doc.toRange(problem.getOffset(), problem.getLength());
						d.setRange(rng);
						d.setSeverity(severity);
						List<QuickfixData<?>> fixes = problem.getQuickfixes();
						if (CollectionUtil.hasElements(fixes)) {
							for (QuickfixData<?> fix : fixes) {
								quickfixes.add(new Quickfix<>(EXTENSION_ID, rng, fix));
							}
						}
						diagnostics.add(d);
					}
				} catch (BadLocationException e) {
					LOG.log(Level.WARNING, "Invalid reconcile problem ignored", e);
				}
			}
		};

		// Avoid running in the same thread as lsp4j as it can result
		// in long "hangs" for slow reconcile providers
		Mono.fromRunnable(() -> {
			engine.reconcile(doc, problems);
		})
		.doOnTerminate((ignore1, ignore2) -> {
			reconcileSession.complete(null);
		})
		.subscribeOn(RECONCILER_SCHEDULER)
		.subscribe();
	}

	protected DiagnosticSeverity getDiagnosticSeverity(ReconcileProblem problem) {
		ProblemSeverity severity = problem.getType().getDefaultSeverity();
		switch (severity) {
		case ERROR:
			return DiagnosticSeverity.Error;
		case WARNING:
			return DiagnosticSeverity.Warning;
		case IGNORE:
			return null;
		default:
			throw new IllegalStateException("Bug! Missing switch case?");
		}
	}

	public void waitForReconcile() throws Exception {
		while (!this.busyReconcile.isDone()) {
			this.busyReconcile.get();
		}
	}

	public LanguageClient getClient() {
		return client;
	}

	public ProgressService getProgressService() {
		return progressService;
	}

	@JsonRequest("sts/quickfix")
	public CompletableFuture<WorkspaceEdit> quickfixResolve(QuickfixResolveParams params) {
		QuickfixRegistry quickfixes = getQuickfixRegistry();
		return quickfixes.handle(params);
	}


}
