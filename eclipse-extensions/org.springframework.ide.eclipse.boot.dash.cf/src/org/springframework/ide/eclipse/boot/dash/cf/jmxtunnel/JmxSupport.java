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
package org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel;

import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.cf.client.SshClientSupport;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnel;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnelFactory;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.launch.util.PortFinder;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

/**
 * Helper class providing functionality to connect to JMX on a remote spring boot app
 * running on CF, using ssh tunneling.
 * <p>
 * The main responsiblity of this class is to manage tunnel life-cycle based on the
 * app's state. (I.e. ensure tunnel is created when app started and tunnel is closed
 * when app stopped or deleted).
 *
 * @author Kris De Volder
 */
public class JmxSupport {

	private static final String JAVA_OPTS = "JAVA_OPTS";

	private static final String JMX_OPTION_PAT =
			"-D(com\\.sun\\.management\\.jmxremote|java\\.rmi\\.server|spring\\.jmx)\\.[a-z\\.]*=\\S*\\s*";

	private static final String JMX_ARGS(int port) {
		return "-Dcom.sun.management.jmxremote.ssl=false " +
			   "-Dcom.sun.management.jmxremote.authenticate=false " +
			   "-Dcom.sun.management.jmxremote.port="+port+" " +
			   "-Dcom.sun.management.jmxremote.rmi.port="+port+" " +
			   "-Djava.rmi.server.hostname=localhost " +
			   "-Dcom.sun.management.jmxremote.local.only=false "+
			   "-Dspring.jmx.enabled=true";
	}

	private CloudAppDashElement app;
	private LiveExpression<Integer> tunnelPort;

	private SshTunnel sshTunnel;
	private boolean disposed;

	private JmxSshTunnelManager tunnels;
	private SshTunnelFactory tunnelFactory;

	public JmxSupport(CloudAppDashElement cde, JmxSshTunnelManager tunnels, SshTunnelFactory tunnelFactory) {
		this.tunnelFactory = tunnelFactory;
		this.app = cde;
		this.tunnels = tunnels;
		this.tunnelPort = new AsyncLiveExpression<Integer>(null, "Update SSH JMX Tunnel for "+app.getName()) {
			{
				dependsOn(app.getBaseRunStateExp());
			}

			@Override
			protected Integer compute() {
				RunState runState = app.getBaseRunStateExp().getValue();
				if (runState == RunState.RUNNING || runState == RunState.DEBUGGING) {
					if (app.getEnableJmxSshTunnel()) {
						return app.getCfJmxPort();
					}
				}
				return -1;
			}
		};

		this.tunnelPort.addListener((exp, v) -> {
			Integer port = exp.getValue();
			if (port!=null && port>0) {
				ensureTunnel(port);
			} else {
				closeTunnel(false);
			}
		});
		cde.onDispose(d -> closeTunnel(true));
	}

	private synchronized void ensureTunnel(Integer port) {
		if (sshTunnel==null && !disposed) {
			createSshTunnel(port);
		}
	}

	private synchronized void closeTunnel(boolean disposed) {
		this.disposed = this.disposed || disposed;
		if (sshTunnel!=null) {
			sshTunnel.close();
			sshTunnel = null;
		}
	}

	public int getPort() {
		int port = app.getCfJmxPort();
		if (port<=0) {
			try {
				//TODO: should we work harder at allocating a unique port accross all remote and local
				// apps? Right now there is a small chance that two apps will get the same port.
				port = PortFinder.findFreePort();
				app.setCfJmxPort(port);
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return port;
	}

	public void setupEnvVars(Map<String, String> env) {
		int port = getPort();
		if (port>0) {
			String javaOpts = env.get(JAVA_OPTS);
			if (javaOpts!=null) {
				//Erase old vars
				javaOpts = javaOpts.replaceAll(JMX_OPTION_PAT, "").trim();
			} else {
				javaOpts = "";
			}
			String jmxArgs = JMX_ARGS(port);
			if ("".equals(javaOpts)) {
				// no other java opts yet
				javaOpts = jmxArgs;
			} else {
				javaOpts = javaOpts + " " +jmxArgs;
			}
			env.put(JAVA_OPTS, javaOpts);
		}
	}

	private void createSshTunnel(Integer port) {
		if (port!=null && port>0) {
			try {
				app.log("Fetching JMX SSH tunnel parameters...");
				SshClientSupport sshInfo = app.getTarget().getSshClientSupport();
				SshHost sshHost = sshInfo.getSshHost();
				String sshUser = sshInfo.getSshUser(app.getAppGuid(), 0);
				String sshCode = sshInfo.getSshCode();
				int remotePort = port;

				app.log("JMX SSH tunnel parameters:");
				app.log("  host: "+sshHost);
				app.log("  user: "+sshUser);
				app.log("  code: "+sshCode);
				app.log("  remote port: "+remotePort);

				//2: create tunnel
				app.log("Creating JMX SSH tunnel...");
				this.sshTunnel = tunnelFactory.create(sshHost, sshUser, sshCode, remotePort, app, remotePort);
				String jmxUrl = getJmxUrl(remotePort);
				app.log("JMX SSH tunnel URL = "+jmxUrl);
				tunnels.add(sshTunnel, app);
			} catch (Exception e) {
				app.setError(e);
				app.log(ExceptionUtil.getMessage(e));
				Log.log(e);
			}
		}
	}

	public static String getJmxUrl(Integer port) {
		if (port!=null && port>0) {
			return "service:jmx:rmi://localhost:"+port+"/jndi/rmi://localhost:"+port+"/jmxrmi";
		}
		return null;
	}

	public boolean isTunnelActive() {
		SshTunnel sshTunnel = this.sshTunnel;
		return sshTunnel != null && !sshTunnel.isDisposed();
	}
}
