/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.lsp4e.server.StreamConnectionProvider;

public class SpringBootLanguageServerViaSocket implements StreamConnectionProvider {
	
	private OutputStream outputStream;
	private InputStream inputStream;
	private Socket socket;
	private int port;

	public SpringBootLanguageServerViaSocket(int port) {
		this.port = port;
	}

	@Override
	public void start() throws IOException {
		try {
			socket = new Socket("localhost", port);
			outputStream = socket.getOutputStream();
			inputStream = socket.getInputStream();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public OutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public InputStream getErrorStream() {
		return null;
	}

	@Override
	public void stop() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
