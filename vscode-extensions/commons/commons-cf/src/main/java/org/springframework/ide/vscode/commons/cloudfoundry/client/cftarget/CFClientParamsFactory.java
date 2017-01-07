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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

/**
 * Resolves Cloud Foundry client parameters from registered params providers
 *
 */
public class CFClientParamsFactory {

	public static final CFClientParamsFactory INSTANCE = new CFClientParamsFactory();

	@SuppressWarnings("unchecked")
	private List<Provider<List<CFClientParams>>> providers = new ArrayList<>();

	private CFClientParamsFactory() {
		// Singleton
		addProvider(new CfCliParamsProvider());
	}

	/**
	 * Adds provider to the front of the registered providers such that it would
	 * be called first when a request is made for CF client params
	 * 
	 * @param provider
	 */
	public void addProvider(Provider<List<CFClientParams>> provider) {
		if (provider != null) {
			providers.add(provider);
		}
	}

	public List<CFClientParams> getParams() {

		// Start from the last provider, which is the highest priority
		for (int i = providers.size() - 1; i >= 0; i--) {
			List<CFClientParams> params = providers.get(i).get();
			if (params != null && !params.isEmpty()) {
				return params;
			}
		}
		return null;

	}

}
