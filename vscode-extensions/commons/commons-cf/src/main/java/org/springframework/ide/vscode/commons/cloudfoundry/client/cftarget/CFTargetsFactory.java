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

import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.ClientRequests;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CloudFoundryClientFactory;

/**
 * Creates targets given a client parameters provider and a client factory.
 *
 */
public class CFTargetsFactory {

	private final CloudFoundryClientFactory clientFactory;
	private final ClientParamsProvider paramsProvider;
	private final ClientTimeouts timeouts;

	public CFTargetsFactory(ClientParamsProvider paramsProvider, CloudFoundryClientFactory clientFactory, ClientTimeouts timeouts) {
		this.clientFactory = clientFactory;
		this.paramsProvider = paramsProvider;
		this.timeouts = timeouts;
	}

	/**
	 * 
	 * @return up-to-date list of CF targets.
	 * @throws Exception
	 */
	public List<CFTarget> getTargets() throws Exception {
		List<CFClientParams> allParams = paramsProvider.getParams();
		List<CFTarget> targets = new ArrayList<>();
		if (allParams != null) {
			for (CFClientParams parameters : allParams) {
				ClientRequests requests = clientFactory.getClient(parameters, timeouts);
				if (requests != null) {
					targets.add(new CFTarget(parameters, requests, getTargetName(parameters)));
				}
			}
		}
		return targets;
	}
	
	public String noTargetsMessage() {
		return paramsProvider.noParamsAvailableMessage();
	}

	protected String getTargetName(CFClientParams params) {
		return labelFromCfApi(params.getApiUrl());
	}

	public static String labelFromCfApi(String cfApiUrl) {
		if (cfApiUrl.startsWith("https://")) {
			return cfApiUrl.substring("https://".length());
		} else if (cfApiUrl.startsWith("http://")) {
			return cfApiUrl.substring("http://".length());
		} else {
			return cfApiUrl;
		}
	}
}
