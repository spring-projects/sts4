/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client;

/**
 * Info object containing various bits of info about the host to which an ssh
 * client may wish to connect.
 *
 * @author Kris De Volder
 */
public class SshHost {

	final private String host;

	final private int port;

	final private String fingerPrint;

	public SshHost(String host, int port, String fingerPrint) {
		super();
		this.host = host;
		this.port = port;
		this.fingerPrint = fingerPrint;
	}

	public String getFingerPrint() {
		return fingerPrint;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}

	@Override
	public String toString() {
		return "SshHost [host=" //$NON-NLS-1$
				+ host + ", port=" //$NON-NLS-1$
				+ port + ", fingerPrint=" //$NON-NLS-1$
				+ fingerPrint + "]";//$NON-NLS-1$
	}
}
