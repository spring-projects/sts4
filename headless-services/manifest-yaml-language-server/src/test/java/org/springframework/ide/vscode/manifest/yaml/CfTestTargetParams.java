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
package org.springframework.ide.vscode.manifest.yaml;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFCredentials;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Kris De Volder
 */
public class CfTestTargetParams {

	public final String api;
	public final CFCredentials credentials;
	public final String space;
	public final String org;

	public CfTestTargetParams(String api, CFCredentials credentials, String org, String space) {
		this.api = api;
		this.credentials = credentials;
		this.org = org;
		this.space = space;
	}

	public static CfTestTargetParams fromEnv() {
		try {
			String refreshToken = fromEnv("CF_TEST_REFRESH_TOKEN");
			CFCredentials credentials = CFCredentials.fromRefreshToken(refreshToken);
			return new CfTestTargetParams(fromEnv("CF_TEST_API_URL"), credentials, fromEnv("CF_TEST_ORG"),
					fromEnvOrFile("CF_TEST_SPACE"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String fromEnvOrFile(String name) throws IOException {
		String envVal = getEnvMaybe(name);
		if (envVal != null) {
			return envVal;
		} else {
			String fileName = fromEnv(name + "_FILE");
			Path path = FileSystems.getDefault().getPath(fileName);
			return Files.readAllLines(path).get(0);
		}
	}

	private static String getEnvMaybe(String name) {
		String val = System.getenv(name);
		if (!StringUtils.hasText(val)) {
			val = System.getenv("bamboo_" + name);
		}
		return val;
	}

	public static String fromEnv(String name) {
		String value = getEnvMaybe(name);
		Assert.isLegal(StringUtils.hasText(value), "The environment varable '" + name + "' must be set");
		return value;
	}

	private static boolean fromEnvBoolean(String name) {
		String value = getEnvMaybe(name);
		if (value == null) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}

}
