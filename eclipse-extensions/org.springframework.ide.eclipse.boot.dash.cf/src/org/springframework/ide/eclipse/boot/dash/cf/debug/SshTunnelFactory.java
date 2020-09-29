/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.debug;

import org.springframework.ide.eclipse.boot.dash.cf.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.util.LogSink;

@FunctionalInterface
public interface SshTunnelFactory {

	SshTunnel create(
			SshHost sshHost,
			String user,
			String oneTimeCode,
			int remotePort,
			LogSink log,
			int localPort
	) throws Exception;

}
