/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xterm;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;
import org.osgi.framework.Bundle;


class XtermServiceProcessManager {
	
	private static final int INVALID_PORT = -1;
	
	private static final String NODEJS_SYSTEM_PROPERTY = "org.springframework.xterm.nodejs";
	
	private HttpClient httpClient = HttpClientBuilder.create().build();
	
	private Process process;
	
	private int port = INVALID_PORT;
	
	synchronized void startProcess() throws IOException {
		port = findFreePort();
		Bundle bundle = XtermPlugin.getDefault().getBundle();
		URL url = FileLocator.find(bundle, new Path("/lib/node_modules/node-xterm/terminal-server.js"), null);
//		URL url = FileLocator.find(bundle, new Path("/lib/terminal-server.js"), null);
//		URL url = FileLocator.find(bundle, new Path("/lib/node-xterm-macos"), null);
//		URL url = FileLocator.find(bundle, new Path("/lib/node-xterm-win.exe"), null);
		url = FileLocator.toFileURL(url);
		try {
			File serverJsFile = new File(url.toURI());
			if (serverJsFile == null || !serverJsFile.exists()) {
				throw new IllegalStateException("Cannot find file " + serverJsFile + ". Cannot start xterm service!");
			}
			File nodeJs = null;
			if (System.getProperty(NODEJS_SYSTEM_PROPERTY) != null) {
				nodeJs = new File(System.getProperty(NODEJS_SYSTEM_PROPERTY));
			} else {
				nodeJs = NodeJSManager.getNodeJsLocation();
			}
			if (nodeJs == null || !nodeJs.exists()) {
				throw new IllegalStateException("Cannot find NodeJS executable at '" + nodeJs + "'. Cannot start xterm service!");
			}
			ProcessBuilder builder = new ProcessBuilder(
					nodeJs.toString(),
					serverJsFile.toString(),
					"--server.port=" + port,
					"--terminal.pty.shutdown=delay", // terminal pty process destroyed right after sockets closed
					"--terminal.pty.shutdown-delay=5",
					"--terminal.auto-shutdown.on=true", // terminal app can shutdown itself if not used 
					"--terminal.auto-shutdown.delay=30" // terminal app shuts itself down in not used for 30 sec	
			);
			
			File logFile = Platform.getStateLocation(XtermPlugin.getDefault().getBundle()).append("xterm-log.log").toFile();
			builder.redirectError(logFile);
			builder.redirectOutput(logFile);
			
			process = builder.start();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}
	
	synchronized private void waitUntilStarted(Duration timeout) throws TimeoutException, InterruptedException {
		long start = System.currentTimeMillis();
		long timeoutMillis = timeout.toMillis();
		do {
			if (System.currentTimeMillis() - start > timeoutMillis) {
				throw new TimeoutException("Timed out waiting for Xterm service to start");
			}
			Thread.sleep(150);
		} while (!isStarted(port));
	}
	
	synchronized private boolean isStarted(int port) {
		HttpGet request = new HttpGet("http://localhost:" + port);
		try {
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				return true;
			}
		} catch (IOException e) {
			// Ignore
		}
		return false;
	}
	
	synchronized void stopService() {
		if (process != null && process.isAlive()) {
			if (port > 0) {
				HttpPost request = new HttpPost("http://localhost:" + port + "/shutdown");
				request.setHeader("Content-Type", "application/json");
				try {
					HttpResponse response = httpClient.execute(request);
					int code = response.getStatusLine().getStatusCode();
					if (code != 200) {
						process.destroy();
					}
				} catch (ClientProtocolException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					process.destroy();
				}
			} else {
				process.destroy();
			}
		}
		process = null;
	}
	
	synchronized String serviceUrl(Duration timeout) throws Exception {
		if (port == INVALID_PORT && process != null && process.isAlive()) {
			process.destroy();
			process = null;
		}
		if (process == null || !process.isAlive()) {
			startProcess();
			try {
				waitUntilStarted(timeout);
			} catch (Exception e) {
				this.port = INVALID_PORT;
				throw e;
			}
		}
		return "http://localhost:" + port;
	}
	
	private static int findFreePort() throws IOException {
	    ServerSocket socket = new ServerSocket(0);
	    try {
	        return socket.getLocalPort();
	    } finally {
	        try {
	            socket.close();
	        } catch (IOException e) {
	        }
	    }
	}


}
