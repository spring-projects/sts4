package org.springframework.ide.vscode.commons.languageserver.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.vscode.commons.languageserver.ProgressParams;
import org.springframework.ide.vscode.commons.languageserver.STS4LanguageClient;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.util.Futures;

import reactor.core.publisher.Flux;

/**
 * Abstract base class to implement LanguageServer. Bits and pieces copied from
 * the 'JavaLanguageServer' example which seem generally useful / reusable end up in
 * here so we can try to keep the subclass itself more 'clutter free' and focus on
 * what its really doing and not the 'wiring and plumbing'.
 */
public abstract class SimpleLanguageServer implements LanguageServer, LanguageClientAware {

    private static final Logger LOG = Logger.getLogger(SimpleLanguageServer.class.getName());

    private Path workspaceRoot;

	private SimpleTextDocumentService tds;

	private SimpleWorkspaceService workspace;

	private STS4LanguageClient client;

	@Override
	public void connect(LanguageClient _client) {
		this.client = (STS4LanguageClient) _client;
		Flux.range(1, 10)
		.delay(Duration.ofMillis(2000))
		.doOnNext((i) -> {
			client.progress(new ProgressParams("test-progress", "Progress ["+i+"] ..."));
		})
		.doAfterTerminate(() -> {
			client.progress(new ProgressParams("test-progress", null));
		})
		.subscribe();
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

	protected abstract ServerCapabilities getServerCapabilities();

    @Override
    public CompletableFuture<Object> shutdown() {
    	return Futures.of(null);
    }

    @Override
    public void exit() {
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
	protected void validateWith(TextDocument doc, IReconcileEngine engine) {

		SimpleTextDocumentService documents = getTextDocumentService();
		IProblemCollector problems = new IProblemCollector() {

			private List<Diagnostic> diagnostics = new ArrayList<>();

			@Override
			public void endCollecting() {
				documents.publishDiagnostics(doc, diagnostics);
			}

			@Override
			public void beginCollecting() {
				diagnostics.clear();
			}

			@Override
			public void accept(ReconcileProblem problem) {
				DiagnosticSeverity severity = getDiagnosticSeverity(problem);
				if (severity!=null) {
					Diagnostic d = new Diagnostic();
					d.setCode(problem.getCode());
					d.setMessage(problem.getMessage());
					d.setRange(doc.toRange(problem.getOffset(), problem.getLength()));
					d.setSeverity(severity);
					diagnostics.add(d);
				}
			}

			private DiagnosticSeverity getDiagnosticSeverity(ReconcileProblem problem) {
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
		};
		engine.reconcile(doc, problems);
	}

	public LanguageClient getClient() {
		return client;
	}
}
