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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfo;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public class CfJson {

	private Gson gson = new Gson();
	private final Logger log = LoggerFactory.getLogger(CfJson.class);

	public CfTargetsInfo from(Settings settings) {
		try {
			JsonElement rawData = settings.getRawSettings();
			if (rawData != null) {
				return gson.fromJson(rawData, CfTargetsInfo.class);
			}
		} catch (JsonSyntaxException e) {
			log.error("", e);
		}
		return null;
	}

	public CfTargetsInfo from(String rawJson) {
		return gson.fromJson(rawJson, CfTargetsInfo.class);
	}

}
