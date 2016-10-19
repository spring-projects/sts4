package org.springframework.ide.vscode.commons.languageserver.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

import io.typefox.lsapi.DiagnosticSeverity;
import io.typefox.lsapi.InitializeParams;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.MessageParams;
import io.typefox.lsapi.MessageType;
import io.typefox.lsapi.ShowMessageRequestParams;
import io.typefox.lsapi.impl.DiagnosticImpl;
import io.typefox.lsapi.impl.InitializeResultImpl;
import io.typefox.lsapi.impl.MessageParamsImpl;
import io.typefox.lsapi.impl.ServerCapabilitiesImpl;
import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.WindowService;

/**
 * Abstract base class to implement LanguageServer. Bits and pieces copied from
 * the 'JavaLanguageServer' example which seem generally useful / reusable end up in
 * here so we can try to keep the subclass itself more 'clutter free' and focus on
 * what its really doing and not the 'wiring and plumbing'.
 */
public abstract class SimpleLanguageServer implements LanguageServer {

    private static final Logger LOG = Logger.getLogger(SimpleLanguageServer.class.getName());

    private Consumer<MessageParams> showMessage = m -> {};

    private Path workspaceRoot;

	private SimpleTextDocumentService tds;

	private SimpleWorkspaceService workspace;

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

        InitializeResultImpl result = new InitializeResultImpl();

        ServerCapabilitiesImpl cap = getServerCapabilities();
        result.setCapabilities(cap);

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public WindowService getWindowService() {
        return new WindowService() {
            @Override
            public void onShowMessage(Consumer<MessageParams> callback) {
                showMessage = callback;
            }

            @Override
            public void onShowMessageRequest(Consumer<ShowMessageRequestParams> callback) {

            }

            @Override
            public void onLogMessage(Consumer<MessageParams> callback) {

            }
        };
    }

    public void onError(String message, Throwable error) {
        if (error instanceof ShowMessageException)
            showMessage.accept(((ShowMessageException) error).message);
        else {
            LOG.log(Level.SEVERE, message, error);

            MessageParamsImpl m = new MessageParamsImpl();

            m.setMessage(message);
            m.setType(MessageType.Error);

            showMessage.accept(m);
        }
    }

	protected abstract ServerCapabilitiesImpl getServerCapabilities();

    @Override
    public void shutdown() {
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
		return new SimpleTextDocumentService();
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

	@Override
	public void onTelemetryEvent(Consumer<Object> callback) {
		//TODO: not sure what this is for exactly. We just stub it and do nothing for now.
	}

	/**
	 * Convenience method. Subclasses can call this to use a {@link IReconcileEngine} ported
	 * from old STS codebase to validate a given {@link TextDocument} and publish Diagnostics.
	 */
	protected void validateWith(TextDocument doc, IReconcileEngine engine) {

		SimpleTextDocumentService documents = getTextDocumentService();
		IProblemCollector problems = new IProblemCollector() {

			private List<DiagnosticImpl> diagnostics = new ArrayList<>();

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
					DiagnosticImpl d = new DiagnosticImpl();
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
}
