/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.debug;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.util.LogSink;
import org.springsource.ide.eclipse.commons.livexp.core.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.keepalive.KeepAliveRunner;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;

/**
 * This class is responsible for creating an ssh tunnel to a remote port. This class implements
 * Closeable, its close method must be called to close the tunnel and avoid resource leak.
 */
public class SshTunnelImpl extends AbstractDisposable implements SshTunnel {

	private boolean closeRequested = false;
	private int localPort;
	private SSHClient ssh;
	private LocalPortForwarder portForwarder;

	public SshTunnelImpl(SshHost sshHost, String user, String oneTimeCode, int remotePort, LogSink log) throws Exception {
		this(sshHost, user, oneTimeCode, remotePort, log, 0);
	}

	public SshTunnelImpl(SshHost sshHost, String user, String oneTimeCode, int remotePort, LogSink log, int _localPort) throws Exception {
		DefaultConfig config = new DefaultConfig();
		config.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE); // Hopefuly this is better at detecting dropped connections from server (e.g. when app is stopped or dies).
		ssh = new SSHClient(config);
		ssh.addHostKeyVerifier(sshHost.getFingerPrint());
		log.log("Ssh client created");

		ssh.connect(sshHost.getHost(), sshHost.getPort());
		ssh.authPassword(user, oneTimeCode);
		KeepAliveRunner keepAlive = (KeepAliveRunner) ssh.getConnection().getKeepAlive();
		keepAlive.setKeepAliveInterval(5);
		keepAlive.setMaxAliveCount(1);
		log.log("Ssh client connected");

		ServerSocket ss = new ServerSocket(_localPort);
		ss.setSoTimeout(5_000);
		localPort = ss.getLocalPort();
		Job job = new Job("SshTunnel port forwarding") {

			@Override
			protected IStatus run(IProgressMonitor arg0) {
				final LocalPortForwarder.Parameters params = new LocalPortForwarder.Parameters("0.0.0.0", localPort, "localhost", remotePort);
				try {
					portForwarder = ssh.newLocalPortForwarder(params, ss);
					boolean retry;
					do {
						retry = false;
						try {
							portForwarder.listen();
						} catch (IOException e) {
							if (!closeRequested) {
								if (e instanceof SocketTimeoutException) {
									//don't log it happens all the time and is expected
								} else {
									log.log(ExceptionUtil.getMessage(e));
								}
								retry = true;
							}
						}
					} while (retry);
				} finally {
					try {
						ss.close();
					} catch (IOException e) {
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
		log.log("Ssh tunnel created: localPort = "+localPort);
	}

	@Override
	synchronized public void dispose() {
		if (portForwarder!=null) {
			try {
				portForwarder.close();
			} catch (Exception e) {
			}
			portForwarder = null;
		}
		if (ssh!=null) {
			try {
				ssh.disconnect();
			} catch (Exception e) {
			}
			ssh = null;
		}
		super.dispose();
	}

	public int getLocalPort() {
		return localPort;
	}


}
