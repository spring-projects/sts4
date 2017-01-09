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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;

import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides existing Cloud Foundry client params, like target and credentials,
 * from the CLI config.json in the file system.
 * 
 *
 */
public class CfCliParamsProvider implements Provider<List<CFClientParams>> {

	public static final String TARGET = "Target";
	public static final String REFRESH_TOKEN = "RefreshToken";
	public static final String ORGANIZATION_FIELDS = "OrganizationFields";
	public static final String SPACE_FIELDS = "SpaceFields";
	public static final String NAME = "Name";
	public static final String SSL_DISABLED = "SSLDisabled";

	private static Logger logger = Logger.getLogger(CfCliParamsProvider.class.getName());

	@Override
	public List<CFClientParams> get() {
		try {
			File file = getConfigJsonFile();
			if (file != null) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> userData = mapper.readValue(file, Map.class);
				if (userData != null) {
					String refreshToken = (String) userData.get(REFRESH_TOKEN);
					// Only support connecting to CF via refresh token for now
					if (refreshToken == null) {
						return null;
					}
					CFCredentials credentials = CFCredentials.fromRefreshToken(refreshToken);
					boolean sslDisabled = (Boolean) userData.get(SSL_DISABLED);
					String target = (String) userData.get(TARGET);
					Map<String, Object> orgFields = (Map<String, Object>) userData.get(ORGANIZATION_FIELDS);
					Map<String, Object> spaceFields = (Map<String, Object>) userData.get(SPACE_FIELDS);
					if (target != null && orgFields != null && spaceFields != null) {
						String orgName = (String) orgFields.get(NAME);
						String spaceName = (String) spaceFields.get(NAME);
						List<CFClientParams> params = new ArrayList<>();
						params.add(new CFClientParams(target, null, credentials, orgName, spaceName, sslDisabled));
						return params;
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			log(e);
		}

		return null;
	}

	private File getConfigJsonFile() throws IOException, InterruptedException {
		// Support Unix systems for now
		if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
			String homeDir = getUnixHomeDir();
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
		}
		return null;
	}

	private String getUnixHomeDir() throws IOException, InterruptedException {
		File currentWorkDir = null; /*
									 * use current working dir which means pass
									 * null
									 */
		ExternalProcess homeDirProcess = new ExternalProcess(currentWorkDir,
				new ExternalCommand("/bin/bash", "-c", "echo $HOME"), true);
		String homeDir = homeDirProcess.getOut();
		if (homeDir != null) {
			homeDir = homeDir.trim(); // remove any new line chars
		}
		return homeDir;
	}

	private void log(Exception e) {
		logger.log(Level.SEVERE, e.getMessage(), e);
	}

}
