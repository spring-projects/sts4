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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnel;
import org.springframework.ide.eclipse.boot.dash.cf.model.CloudAppDashElement;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.boot.ls.remoteapps.RemoteBootAppsDataHolder.RemoteAppData;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import com.google.common.collect.ImmutableSet;

/**
 * An instance of this class keeps track of open ssh tunnels for JMX connections to
 * remote hosts. It is capable of producing a (Live)Set of JMX urls for all the
 * open tunnels.
 */
public class JmxSshTunnelManager {

	private Map<SshTunnel, CloudAppDashElement> tunnels = new HashMap<>();

	private ObservableSet<RemoteAppData> jmxUrls = ObservableSet.<RemoteAppData>builder()
			.refresh(AsyncMode.ASYNC)
			.compute(this::collectUrls)
			.build();

	private synchronized ImmutableSet<RemoteAppData> collectUrls() {
		ImmutableSet.Builder<RemoteAppData> builder = ImmutableSet.builder();
		for (Entry<SshTunnel, CloudAppDashElement> entry : tunnels.entrySet()) {
			SshTunnel tunnel = entry.getKey();
			CloudAppDashElement app = entry.getValue();
			int port = tunnel.getLocalPort();
			if (port>0) {
				RemoteAppData data = new RemoteAppData();
				data.setJmxurl(JmxSupport.getJmxUrl(port));
				data.setHost(app.getLiveHost());
				data.setKeepChecking(false);
				UUID guid = app.getAppGuid();
				if (guid!=null) {
					data.setProcessId(guid.toString());
				}
				builder.add(data);
			}
		}
		return builder.build();
	}

	public void add(SshTunnel sshTunnel, CloudAppDashElement app) {
		sshTunnel.onDispose(this::handleTunnelClosed);
		tunnels.put(sshTunnel, app);
		jmxUrls.refresh();
		app.getJmxSshTunnelStatus().refresh();
	}

	private void handleTunnelClosed(Disposable disposed) {
		CloudAppDashElement owner = tunnels.remove(disposed);
		owner.getJmxSshTunnelStatus().refresh();
		jmxUrls.refresh();
	}

	public ObservableSet<RemoteAppData> getUrls() {
		return jmxUrls;
	}

}
