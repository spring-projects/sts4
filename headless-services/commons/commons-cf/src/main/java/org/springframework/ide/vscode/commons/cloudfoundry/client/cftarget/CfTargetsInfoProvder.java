/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * CF Client parameter provider based on CF targets info. These targets info contain
 * a list of CF targets that typically already exist and contain connection information
 * like refresh token or other type of credentials. For example, it allows external clients
 * to integrate with the manifest LS by passing target information from that client to the LS.
 * 
 * @author Alex Boyko
 *
 */
public class CfTargetsInfoProvder implements ClientParamsProvider {
	
	private static final String NO_TARGETS_FOUND_MESSAGE = "No targets found";
	private static final String NO_NETWORK_CONNECTION = "No connection to Cloud Foundry";
	private static final String NO_ORG_SPACE = "No org/space selected";
	
	private static final String PROP_NO_TARGETS_FOUND = "noTargetsFound";
	private static final String PROP_UNAUTHORISED = "unauthorised";
	private static final String PROP_NO_NETWORK_CONNECTION = "noNetworkConnection";
	private static final String PROP_NO_ORG_SPACE = "noOrgSpace";
	
	private Supplier<Collection<CFClientParams>> paramsSupplier;
	private Map<String, String> messages;
	
	public CfTargetsInfoProvder(CfTargetsInfo targetsInfo) {
		this.messages = targetsInfo.getCfDiagnosticMessages();
		this.paramsSupplier = Suppliers.memoize(() -> targetsInfo.getCfTargets()
				.stream()
				.map(t -> parseCfClientParams(t))
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));
	}

	@Override
	public Collection<CFClientParams> getParams() throws NoTargetsException, ExecutionException {
		Collection<CFClientParams> params = paramsSupplier.get();
		if (params == null || params.isEmpty()) {
			throw new NoTargetsException(getMessages().noTargetsFound());
		}
		return params;
	}
	
	private static CFClientParams parseCfClientParams(CfTargetsInfo.Target target) {
		String refreshToken = target.getRefreshToken();
		// Only support connecting to CF via refresh token for now
		if (StringUtil.hasText(refreshToken)) {
			CFCredentials credentials = CFCredentials.fromRefreshToken(refreshToken);
			boolean sslDisabled = target.getSslDisabled();
			String api = target.getApi();
			String orgName = target.getOrg();
			String spaceName = target.getSpace();
			if (api != null && StringUtil.hasText(orgName) && StringUtil.hasText(spaceName)) {
				return new CFClientParams(api, null, credentials, orgName, spaceName, sslDisabled);
			}
		}
		return null;
	}

	@Override
	public CFParamsProviderMessages getMessages() {
		return new CFParamsProviderMessages() {

			@Override
			public String noTargetsFound() {
				if (messages != null && messages.containsKey(PROP_NO_TARGETS_FOUND)) {
					return messages.get(PROP_NO_TARGETS_FOUND);
				}
				return NO_TARGETS_FOUND_MESSAGE;
			}

			@Override
			public String unauthorised() {
				if (messages != null && messages.containsKey(PROP_UNAUTHORISED)) {
					return messages.get(PROP_UNAUTHORISED);
				}
				return NO_NETWORK_CONNECTION;
			}

			@Override
			public String noNetworkConnection() {
				if (messages != null && messages.containsKey(PROP_NO_NETWORK_CONNECTION)) {
					return messages.get(PROP_NO_NETWORK_CONNECTION);
				}
				return NO_NETWORK_CONNECTION;
			}

			@Override
			public String noOrgSpace() {
				if (messages != null && messages.containsKey(PROP_NO_ORG_SPACE)) {
					return messages.get(PROP_NO_ORG_SPACE);
				}
				return NO_ORG_SPACE;
			}

		};
	}

}
