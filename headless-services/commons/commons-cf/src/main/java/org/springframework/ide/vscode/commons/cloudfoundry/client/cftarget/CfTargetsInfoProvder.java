/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfo.TargetDiagnosticMessages;
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
	public static final String NO_NETWORK_CONNECTION = "No connection to Cloud Foundry";
	private static final String NO_ORG_SPACE = "No org/space selected";
	public static final TargetDiagnosticMessages DEFAULT_MESSAGES = new TargetDiagnosticMessages() {

		@Override
		public String getNoTargetsFound() {
			return NO_TARGETS_FOUND_MESSAGE;
		}

		@Override
		public String getConnectionError() {
			return NO_NETWORK_CONNECTION;
		}

		@Override
		public String getNoOrgSpace() {
			return NO_ORG_SPACE;
		}
	};
		
	private Supplier<Collection<CFClientParams>> paramsSupplier;
	private TargetDiagnosticMessages messages;

	public CfTargetsInfoProvder(CfTargetsInfo targetsInfo) {
		this.messages = targetsInfo.getDiagnosticMessages();
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
			throw new NoTargetsException(getMessages().getNoTargetsFound());
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
	public TargetDiagnosticMessages getMessages() {
		return messages != null ? messages : DEFAULT_MESSAGES;
	}
}
