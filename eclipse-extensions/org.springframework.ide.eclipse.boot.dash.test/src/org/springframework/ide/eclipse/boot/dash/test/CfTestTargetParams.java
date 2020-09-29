/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;

/**
 * @author Kris De Volder
 */
public class CfTestTargetParams {

	public static CFClientParams fromEnv() {
		try {
			return new CFClientParams(
					fromEnv("CF_TEST_API_URL"),
					fromEnv("CF_TEST_USER"),
					CFCredentials.fromPassword(fromEnv("CF_TEST_PASSWORD")),
					false, //self signed
					fromEnv("CF_TEST_ORG"),
					fromEnvOrFile("CF_TEST_SPACE"),
					fromEnvBoolean("CF_TEST_SKIP_SSL")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String fromEnvOrFile(String name) throws IOException {
		String envVal = getEnvMaybe(name);
		if (envVal!=null) {
			return envVal;
		} else {
			String fileName = fromEnv(name+"_FILE");
			Path path = FileSystems.getDefault().getPath(fileName);
			return Files.readAllLines(path).get(0);
		}
	}

	private static String getEnvMaybe(String name) {
		String val = System.getenv(name);
		if (!StringUtils.hasText(val)) {
			val = System.getenv("bamboo_"+name);
		}
		return val;
	}

	public static String fromEnv(String name) {
		String value = getEnvMaybe(name);
		Assert.isLegal(StringUtils.hasText(value), "The environment varable '"+name+"' must be set");
		return value;
	}

	private static boolean fromEnvBoolean(String name) {
		String value = getEnvMaybe(name);
		if (value == null) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	public static CFClientParams fromEnvWithCredentials(CFCredentials credentials) {
		try {
			return new CFClientParams(
					fromEnv("CF_TEST_API_URL"),
					fromEnv("CF_TEST_USER"),
					credentials,
					false, //self signed
					fromEnv("CF_TEST_ORG"),
					fromEnvOrFile("CF_TEST_SPACE"),
					fromEnvBoolean("CF_TEST_SKIP_SSL")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
