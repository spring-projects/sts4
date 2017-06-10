/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import org.springframework.ide.vscode.commons.util.ListenerList;

/**
 * Default CF client configuration implementation
 * 
 * @author Alex Boyko
 *
 */
class CfClientConfigImpl implements CfClientConfig {
	
	private ClientParamsProvider clientParamsProvider;
	private ListenerList<ClientParamsProviderChangedListener> listeners = new ListenerList<>();

	@Override
	public ClientParamsProvider getClientParamsProvider() {
		return clientParamsProvider;
	}

	@Override
	public void setClientParamsProvider(ClientParamsProvider provider) {
		ClientParamsProvider oldParamsProvider = this.clientParamsProvider;
		this.clientParamsProvider = provider;
		listeners.forEach(l -> l.clientParamsProviderChanged(clientParamsProvider, oldParamsProvider));
	}

	@Override
	public void addClientParamsProviderChangedListener(ClientParamsProviderChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeClientParamsProviderChangedListener(ClientParamsProviderChangedListener listener) {
		listeners.remove(listener);
	}

}
