/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.springframework.ide.vscode.commons.languageserver.STS4LanguageClient;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

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
 * 
 * @author Martin Lippert
 */
public class StartupServer {

	public static void main(String[] args) throws InterruptedException, IOException {

		JavaProjectFinder javaProjectFinder = BootJavaLanguageServer.DEFAULT_PROJECT_FINDER;
		SimpleLanguageServer languageServer = new BootJavaLanguageServer(javaProjectFinder);

		Function<MessageConsumer, MessageConsumer> wrapper = consumer -> {
			MessageConsumer result = consumer;
			return result;
		};
		
		Launcher<STS4LanguageClient> launcher = createSocketLauncher(languageServer, STS4LanguageClient.class, new InetSocketAddress("localhost", 5007), Executors.newCachedThreadPool(), wrapper);

		languageServer.connect(launcher.getRemoteProxy());
		Future<?> future = launcher.startListening();
		while (!future.isDone()) {
			Thread.sleep(10_000l);
		}
	}
	
    private static <T> Launcher<T> createSocketLauncher(Object localService, Class<T> remoteInterface, SocketAddress socketAddress, ExecutorService executorService, Function<MessageConsumer, MessageConsumer> wrapper) throws IOException {
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

}
