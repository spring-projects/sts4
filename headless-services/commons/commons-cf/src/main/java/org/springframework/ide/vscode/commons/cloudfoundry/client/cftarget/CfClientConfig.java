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

/**
 * CF Client configuration
 * 
 * @author Alex Boyko
 *
 */
public interface CfClientConfig {
	
	/**
	 * Listener to changes in parameters provider
	 */
	@FunctionalInterface
	public interface ClientParamsProviderChangedListener {
		void clientParamsProviderChanged(ClientParamsProvider newProvider, ClientParamsProvider oldProvider);
	}
	
	/**
	 * Returns CF client parameters provider
	 * @return parameters provider
	 */
	ClientParamsProvider getClientParamsProvider();
	
	/**
	 * Sets CF client parameters provider
	 * @param provider
	 */
	void setClientParamsProvider(ClientParamsProvider provider);
	
	/**
	 * Adds listener to listen to CF parameter provider changes
	 * @param listener
	 */
	void addClientParamsProviderChangedListener(ClientParamsProviderChangedListener listener);
	
	/**
	 * Removes CF parameter provider change listener
	 * @param listener
	 */
	void removeClientParamsProviderChangedListener(ClientParamsProviderChangedListener listener);
	
	/**
	 * Default CF Client configuration
	 */
	static CfClientConfig createDefault() {
		return new CfClientConfigImpl();
	}
}
