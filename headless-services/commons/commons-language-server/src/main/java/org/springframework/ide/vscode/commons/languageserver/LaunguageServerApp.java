/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.inject.Provider;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.LoggingFormat;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Log;


/**
 * Abstract class meant to minimize the amount of code needed to
 * write to create suitable 'main' method to launch a language server.
 * <p>
 * The easiest way to use this is to create your own static main method.
 * Then call this class's start method with a Provider<SimpleLanguageServer> as
 * a argument.
 * <p>
 * Alternatively, you can also subclass it and implement the abstract
 * `createServer` method.
 *
 * @author Kris De Volder
 * @author Martin Lippert
 */
public abstract class LaunguageServerApp {

	public static final String STS4_LANGUAGESERVER_NAME = "sts4.languageserver.name";
	public static final String STANDALONE_STARTUP = "standalone-startup";
	private static final int SERVER_STANDALONE_PORT = 5007;

	public static void start(String name, Provider<SimpleLanguageServer> languageServerFactory) throws IOException, InterruptedException {
		System.setProperty(STS4_LANGUAGESERVER_NAME, name); //makes it easy to recognize language server processes.
		LaunguageServerApp app = new LaunguageServerApp() {
			@Override
			protected SimpleLanguageServer createServer() {
				return languageServerFactory.get();
			}
		};

		if (System.getProperty(STANDALONE_STARTUP, "false").equals("true")) {
			app.startAsServer();
		}
		else {
			app.start();
		}
	}

	public static void startAsServer(Provider<SimpleLanguageServer> languageServerFactory) throws IOException, InterruptedException {
		LaunguageServerApp app = new LaunguageServerApp() {
			@Override
			protected SimpleLanguageServer createServer() {
				return languageServerFactory.get();
			}
		};
		app.startAsServer();
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
					Log.log(e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					Log.log(e);
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					Log.log(e);
				}
			}
		}
	}

	public void start() throws IOException {
		Log.info("Starting LS");
		Connection connection = null;
		try {
			LoggingFormat.startLogging();
			connection = connectToNode();

			run(connection);
		} catch (Throwable t) {
			Log.log(t);
			System.exit(1);
		} finally {
			if (connection != null) {
				connection.dispose();
			}
		}
	}

	/**
	 * starts up the language server and let it listen for connections from the outside
	 * instead of connecting itself to an existing port or channel.
	 *
	 * This is meant for development only, to reduce turnaround times while working
	 * on the language server from within an IDE, so that you can start the language
	 * server right away in debug mode and let the vscode extension connect to that
	 * instance instead of vice versa.
	 *
	 * Source of inspiration:
	 * https://github.com/itemis/xtext-languageserver-example/blob/master/org.xtext.example.mydsl.ide/src/org/xtext/example/mydsl/ide/RunServer.java
	 */
	public void startAsServer() throws IOException, InterruptedException {
		Log.info("Starting LS as standlone server port = "+SERVER_STANDALONE_PORT);

		Function<MessageConsumer, MessageConsumer> wrapper = consumer -> {
			MessageConsumer result = consumer;
			return result;
		};

		SimpleLanguageServer languageServer = createServer();
		Launcher<STS4LanguageClient> launcher = createSocketLauncher(languageServer, STS4LanguageClient.class,
				new InetSocketAddress("localhost", SERVER_STANDALONE_PORT), createServerThreads(), wrapper);

		languageServer.connect(launcher.getRemoteProxy());
		Future<?> future = launcher.startListening();
		while (!future.isDone()) {
			Thread.sleep(10_000l);
		}
	}

	/**
	 * Creates the thread pool / executor passed to lsp4j server intialization. From the looks of things,
	 * @return
	 */
    protected ExecutorService createServerThreads() {
		return Executors.newCachedThreadPool();
	}

	private <T> Launcher<T> createSocketLauncher(Object localService, Class<T> remoteInterface, SocketAddress socketAddress, ExecutorService executorService, Function<MessageConsumer, MessageConsumer> wrapper) throws IOException {
        AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(socketAddress);
        AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = serverSocket.accept().get();
            return Launcher.createIoLauncher(localService, remoteInterface, Channels.newInputStream(socketChannel), Channels.newOutputStream(socketChannel), executorService, wrapper);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Connection connectToNode() throws IOException {
		String port = System.getProperty("server.port");

		if (port != null) {
			Socket socket = new Socket("localhost", Integer.parseInt(port));

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();

			Log.info("Connected to parent using socket on port " + port);
			return new Connection(in, out, socket);
		}
		else {
			InputStream in = System.in;
			PrintStream out = System.out;

			Log.info("Connected to parent using stdio");

			return new Connection(in, out, null);
		}
	}

	/**
	 * Listen for requests from the parent node process.
	 * Send replies asynchronously.
	 * When the request stream is closed, wait for 5s for all outstanding responses to compute, then return.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	protected void run(Connection connection) throws InterruptedException, ExecutionException {
		LanguageServer server = createServer();
		ExecutorService executor = createServerThreads();
		Function<MessageConsumer, MessageConsumer> wrapper = (MessageConsumer consumer) -> {
			return (msg) -> {
				try {
					consumer.consume(msg);
				} catch (UnsupportedOperationException e) {
					//log a warning and ignore. We are getting some messages from vsCode the server doesn't know about
					Log.warn("Unsupported message was ignored!", e);
				}
			};
		};
		Launcher<STS4LanguageClient> launcher = Launcher.createLauncher(server,
				STS4LanguageClient.class,
				connection.in,
				connection.out,
				executor,
				wrapper
		);

		if (server instanceof LanguageClientAware) {
			LanguageClient client = launcher.getRemoteProxy();
			((LanguageClientAware) server).connect(client);
		}

		launcher.startListening().get();
	}

	protected abstract SimpleLanguageServer createServer();

}
