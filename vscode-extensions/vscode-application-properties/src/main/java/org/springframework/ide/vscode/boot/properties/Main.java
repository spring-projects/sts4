/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.ide.vscode.util.LoggingFormat;

import io.typefox.lsapi.services.json.LoggingJsonAdapter;

/**
 * Starts up Language Server process
 * 
 * @author Alex Boyko
 *
 */
public class Main {
    private static final Logger LOG = Logger.getLogger("main");

    public static void main(String[] args) throws IOException {
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

    private static class Connection {
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

    /**
     * Listen for requests from the parent node process.
     * Send replies asynchronously.
     * When the request stream is closed, wait for 5s for all outstanding responses to compute, then return.
     */
    public static void run(Connection connection) {
    	ApplicationPropertiesLanguageServer server = new ApplicationPropertiesLanguageServer();
    	LoggingJsonAdapter jsonServer = new LoggingJsonAdapter(server);
    	jsonServer.setMessageLog(new PrintWriter(System.out));

        jsonServer.connect(connection.in, connection.out);
        jsonServer.getProtocol().addErrorListener((message, err) -> {
            LOG.log(Level.SEVERE, message, err);

            server.onError(message, err);
        });
        jsonServer.join();
    }
}
