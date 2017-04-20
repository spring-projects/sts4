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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceEdit;
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
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.CollectionUtil;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

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

	private static final Scheduler RECONCILER_SCHEDULER = Schedulers.newSingle("Reconciler");

	public final String EXTENSION_ID;
	private final String CODE_ACTION_COMMAND_ID;

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

	private LanguageServerTestListener testListener;

	private boolean hasCompletionSnippetSupport;

	private boolean hasExecuteCommandSupport;

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

	public SnippetBuilder createSnippetBuilder() {
		return new SnippetBuilder();
	}

	public SimpleLanguageServer(String extensionId) {
		this.EXTENSION_ID = extensionId;
		this.CODE_ACTION_COMMAND_ID = "sts."+EXTENSION_ID+".codeAction";
	}

	@Override
	public void initialized() {
		Log.info("Initialized!");
		if (hasExecuteCommandSupport) {
			RegistrationParams params = new RegistrationParams(ImmutableList.of(
				new Registration(
						UUID.randomUUID().toString(),
						"workspace/executeCommand",
						new ExecuteCommandOptions(ImmutableList.of(
								CODE_ACTION_COMMAND_ID
						))
				)
			));
			getWorkspaceService().onExecuteCommand(this::executeCommand);
			Log.info("Registering capabilitie: "+params);
			Mono.fromFuture(client.registerCapability(params))
				.otherwise((e) -> {
					Log.warn("registerCapability failed, using non-standard 'registerFeature' instead.", e);
					return Mono.fromFuture(client.registerFeature(params));
				})
				.doOnError(Log::log)
				.subscribe();
		}
	}

	protected CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		if (CODE_ACTION_COMMAND_ID.equals(params.getCommand())) {
			Assert.isLegal(params.getArguments().size()==2);
			QuickfixResolveParams quickfixParams = new QuickfixResolveParams(
					(String)params.getArguments().get(0), params.getArguments().get(1)
			);
			return quickfixResolve(quickfixParams)
					.then((WorkspaceEdit edit) -> Mono.fromFuture(client.applyEdit(new ApplyWorkspaceEditParams(edit))))
					.map(r -> (Object)r.getApplied())
					.toFuture();
		}
		Log.warn("Unknown command ignored: "+params.getCommand());
		return CompletableFuture.completedFuture(false);
	}

	public Mono<WorkspaceEdit> quickfixResolve(QuickfixResolveParams params) {
		QuickfixRegistry quickfixes = getQuickfixRegistry();
		return quickfixes.handle(params);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		Log.debug("Initializing: "+params);
		String rootPath = params.getRootPath();
		if (rootPath==null) {
			Log.warn("workspaceRoot NOT SET");
		} else {
			this.workspaceRoot= Paths.get(rootPath).toAbsolutePath().normalize();
		}
		this.hasCompletionSnippetSupport = safeGet(false, () -> params.getCapabilities().getTextDocument().getCompletion().getCompletionItem().getSnippetSupport());
		this.hasExecuteCommandSupport = safeGet(false, () -> params.getCapabilities().getWorkspace().getExecuteCommand().getDynamicRegistration());
		Log.info("workspaceRoot = "+workspaceRoot);
		Log.info("hasCompletionSnippetSupport = "+hasCompletionSnippetSupport);
		Log.info("hasExecuteCommandSupport = "+hasExecuteCommandSupport);

		InitializeResult result = new InitializeResult();

		ServerCapabilities cap = getServerCapabilities();
		result.setCapabilities(cap);

		return CompletableFuture.completedFuture(result);
	}

    /**
     * Get some info safely. If there's any kind of exception, ignore it
     * and retutn default value instead.
     */
	private static <T> T safeGet(T deflt, Callable<T> getter) {
		try {
			T x = getter.call();
			if (x!=null) {
				return x;
			}
		} catch (Exception e) {
		}
		return deflt;
	}

	public void onError(String message, Throwable error) {
		LanguageClient cl = this.client;
		if (cl != null) {
			if (error instanceof ShowMessageException)
				client.showMessage(((ShowMessageException) error).message);
			else {
				Log.log(message, error);

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
	protected void validateWith(TextDocumentIdentifier docId, IReconcileEngine engine) {
		CompletableFuture<Void> reconcileSession = this.busyReconcile = new CompletableFuture<Void>();
//		Log.debug("Reconciling BUSY");

		SimpleTextDocumentService documents = getTextDocumentService();

		int requestedVersion = documents.getDocument(docId.getUri()).getVersion();

		// Avoid running in the same thread as lsp4j as it can result
		// in long "hangs" for slow reconcile providers
		Mono.fromRunnable(() -> {
			TextDocument doc = documents.getDocument(docId.getUri()).copy();
			if (requestedVersion!=doc.getVersion()) {
				//Do not bother reconciling if document contents is already stale.
				return;
			}
			if (testListener!=null) {
				testListener.reconcileStarted(docId.getUri(), doc.getVersion());
			}
			IProblemCollector problems = new IProblemCollector() {

				private List<Diagnostic> diagnostics = new ArrayList<>();
				private List<Quickfix> quickfixes = new ArrayList<>();

				@Override
				public void endCollecting() {
					documents.setQuickfixes(docId, quickfixes);
					documents.publishDiagnostics(docId, diagnostics);
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
									quickfixes.add(new Quickfix<>(CODE_ACTION_COMMAND_ID, rng, fix));
								}
							}
							diagnostics.add(d);
						}
					} catch (BadLocationException e) {
						Log.warn("Invalid reconcile problem ignored", e);
					}
				}
			};

//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//			}
			engine.reconcile(doc, problems);
		})
		.otherwise(error -> {
			Log.log(error);
			return Mono.empty();
		})
		.doFinally(ignore -> {
			reconcileSession.complete(null);
//			Log.debug("Reconciler DONE : "+this.busyReconcile.isDone());
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

	/**
	 * If reconciling is in progress, waits until reconciling has caught up to
	 * all the document changes.
	 */
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

	public void setTestListener(LanguageServerTestListener languageServerTestListener) {
		Assert.isLegal(this.testListener==null);
		testListener = languageServerTestListener;
	}


}
