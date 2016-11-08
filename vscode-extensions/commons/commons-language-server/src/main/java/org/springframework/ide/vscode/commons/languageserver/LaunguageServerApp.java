package org.springframework.ide.vscode.commons.languageserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.LoggingFormat;

import com.google.inject.Provider;

/**
 * Abstract class meant to minimize the amount of code needed to
 * write to create suitable 'main' method to launch a language server.
 * <p>
 * The easiest way to use this is to create your own static main method.
 * Then call this class's start method with a Provider<LanguageServer> as
 * a argument.
 * <p>
 * Alternatively, you can also subclass it and implement the abstract 
 * `createServer` method.
 * 
 * @author Kris De Volder
 */
public abstract class LaunguageServerApp {
	
	public static void start(Provider<LanguageServer> languageServerFactory) throws IOException {
		LaunguageServerApp app = new LaunguageServerApp() {
			@Override
			protected LanguageServer createServer() {
				return languageServerFactory.get();
			}
		};
		app.start();
	}

	protected static class Connection {
		final InputStream in;
		final OutputStream out;
		final Socket socket;

		private Connection(InputStream in, OutputStream out, Socket socket) {
			this.in = in;
			this.out = out;
			this.socket = socket;
		}

		void dispose() {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					LOG.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	public void start() throws IOException {
		LOG.info("Starting LS");
		Connection connection = null;
		try {
			LoggingFormat.startLogging();

			connection = connectToNode();

			run(connection);
		} catch (Throwable t) {
			LOG.log(Level.SEVERE, t.getMessage(), t);
			System.exit(1);
		} finally {
			if (connection != null) {
				connection.dispose();
			}
		}
	}

	private static Connection connectToNode() throws IOException {
		String port = System.getProperty("server.port");

		if (port != null) {
			Socket socket = new Socket("localhost", Integer.parseInt(port));

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();

			OutputStream intercept = new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					out.write(b);
				}
			};

			LOG.info("Connected to parent using socket on port " + port);

			return new Connection(in, intercept, socket);
		}
		else {
			InputStream in = System.in;
			PrintStream out = System.out;

			LOG.info("Connected to parent using stdio");

			return new Connection(in, out, null);
		}
	}

	protected static final Logger LOG = Logger.getLogger("main");

	/**
	 * Listen for requests from the parent node process.
	 * Send replies asynchronously.
	 * When the request stream is closed, wait for 5s for all outstanding responses to compute, then return.
	 */
	protected void run(Connection connection) {
		LanguageServer server = createServer();
		boolean validate = false; // not totally sure what it does, disabling it for now.
		Launcher<LanguageClient> launcher = Launcher.createLauncher(server, LanguageClient.class, connection.in, connection.out, validate, new PrintWriter(System.out));

		if (server instanceof LanguageClientAware) {
			LanguageClient client = launcher.getRemoteProxy();
			((LanguageClientAware) server).connect(client);
		}

		launcher.startListening();
	}

	protected abstract LanguageServer createServer();


}
