package org.springframework.ide.vscode.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.typefox.lsapi.InitializeParams;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.MessageParams;
import io.typefox.lsapi.MessageType;
import io.typefox.lsapi.ShowMessageRequestParams;
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


}
