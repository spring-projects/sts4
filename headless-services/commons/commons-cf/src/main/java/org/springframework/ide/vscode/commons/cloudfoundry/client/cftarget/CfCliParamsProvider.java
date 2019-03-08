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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfo.TargetDiagnosticMessages;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Provides existing Cloud Foundry client params, like target and credentials,
 * from the CLI config.json in the file system.
 *
 *
 */
public class CfCliParamsProvider implements ClientParamsProvider {

	public static final String TARGET = "Target";
	public static final String REFRESH_TOKEN = "RefreshToken";
	public static final String ORGANIZATION_FIELDS = "OrganizationFields";
	public static final String SPACE_FIELDS = "SpaceFields";
	public static final String NAME = "Name";
	public static final String SSL_DISABLED = "SSLDisabled";
	
	public static final TargetDiagnosticMessages CLI_PROVIDER_MESSAGES = new TargetDiagnosticMessages() {

		@Override
		public String getNoTargetsFound() {
			// Make this a "generic" message, instead of using "cf CLI" prefix as it shows general instructions when there are not targets
			return "No Cloud Foundry targets found: Use 'cf' CLI to login";
		}

		@Override
		public String getConnectionError() {
			return "cf CLI - Connection error: Verify connection or use 'cf' CLI to login again";
		}

		@Override
		public String getNoOrgSpace() {
			return "cf CLI - No org/space selected: Use 'cf' CLI to login";
		}

		@Override
		public String getTargetSource() {
			return "cf CLI";
		}

	};

	private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	private static CfCliParamsProvider instance;
	
	public static final CfCliParamsProvider getInstance() {
		if (instance == null) {
			instance= new CfCliParamsProvider();
		}
		return instance;
	}

	private CfCliParamsProvider() {
		
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.
	 * ClientParamsProvider#getParams()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CFClientParams> getParams() throws NoTargetsException, ExecutionException {
		List<CFClientParams> params = new ArrayList<>();

		try {
			File file = getConfigJsonFile();
			if (file != null) {
				Map<String, Object> userData = gson.fromJson(new FileReader(file), Map.class);
				if (userData != null) {
					String refreshToken = (String) userData.get(REFRESH_TOKEN);
					// Only support connecting to CF via refresh token for now
					if (isRefreshTokenSet(refreshToken)) {
						CFCredentials credentials = CFCredentials.fromRefreshToken(refreshToken);
						boolean sslDisabled = (Boolean) userData.get(SSL_DISABLED);
						String target = (String) userData.get(TARGET);
						Map<String, Object> orgFields = (Map<String, Object>) userData.get(ORGANIZATION_FIELDS);
						Map<String, Object> spaceFields = (Map<String, Object>) userData.get(SPACE_FIELDS);
						if (target != null && orgFields != null && spaceFields != null) {
							String orgName = (String) orgFields.get(NAME);
							String spaceName = (String) spaceFields.get(NAME);
							if (!StringUtil.hasText(orgName) || !StringUtil.hasText(spaceName)) {
								throw new NoTargetsException(getMessages().getNoOrgSpace());
							}
							params.add(new CFClientParams(target, null, credentials, orgName, spaceName, sslDisabled));
						}
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			throw new ExecutionException(e);
		}

		if (params.isEmpty()) {
			throw new NoTargetsException(getMessages().getNoTargetsFound());
		} else {
			return params;
		}
	}

	private boolean isRefreshTokenSet(String token) {
		return StringUtil.hasText(token);
	}

	private File getConfigJsonFile() throws IOException, InterruptedException {
		String homeDir = getHomeDir();
		if (homeDir != null) {
			if (!homeDir.endsWith("/")) {
				homeDir += '/';
			}
			String filePath = homeDir + ".cf/config.json";
			File file = new File(filePath);
			if (file.exists() && file.canRead()) {
				return file;
			}
		}
		return null;
	}

	private String getHomeDir() throws IOException, InterruptedException {
		return System.getProperty("user.home");
	}

	@Override
	public TargetDiagnosticMessages getMessages() {
		return CLI_PROVIDER_MESSAGES;
	}
}
