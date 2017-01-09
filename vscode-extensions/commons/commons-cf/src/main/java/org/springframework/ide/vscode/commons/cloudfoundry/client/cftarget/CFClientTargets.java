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
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;

/**
 * Creates targets given a client parameters factory and a client factory.
 *
 */
public class CFClientTargets {

	private final CFClientParamsFactory paramsFactory;
	private final CloudFoundryClientFactory clientFactory;

	public CFClientTargets(CFClientParamsFactory paramsFactory, CloudFoundryClientFactory clientFactory) {
		this.paramsFactory = paramsFactory;
		this.clientFactory = clientFactory;
	}

	public List<CFClientTarget> getTargets() throws Exception {
		List<CFClientParams> allParams = paramsFactory.getParams();
		List<CFClientTarget> targets = new ArrayList<>();
		if (allParams != null) {
			for (CFClientParams parameters : allParams) {
				ClientRequests requests = clientFactory.getClient(parameters);
				if (requests != null) {
					targets.add(new CFClientTarget(parameters, requests, getTargetName(parameters)));
				}
			}
		}
		return targets;
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

	public static CFClientTargets createDefaultV2ClientTargets() {
		CFClientParamsFactory paramsFactory = CFClientParamsFactory.INSTANCE;
		CloudFoundryClientFactory clientFactory = DefaultCloudFoundryClientFactoryV2.INSTANCE;
		return new CFClientTargets(paramsFactory, clientFactory);
	}

}
