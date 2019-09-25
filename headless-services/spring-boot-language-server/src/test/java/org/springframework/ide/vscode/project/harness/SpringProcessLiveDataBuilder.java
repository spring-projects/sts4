/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.project.harness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveBeansModel;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveConditional;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveConditionalParser;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveContextPathUtil;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveProperties;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveRequestMapping;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveRequestMappingBoot1xRequestMapping;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;

/**
 * @author Martin Lippert
 */
public class SpringProcessLiveDataBuilder {

	private String processName;
	private String processID;
	
	private String contextPath;
	private String urlScheme;
	private String port;
	private String host;

	private LiveBeansModel beansModel;
	private String[] activeProfiles;
	private LiveRequestMapping[] requestMappings;
	private LiveConditional[] conditionals;
	private LiveProperties properties;
	
	public SpringProcessLiveDataBuilder processName(String processName) {
		this.processName = processName;
		return this;
	}

	public SpringProcessLiveDataBuilder processID(String processID) {
		this.processID = processID;
		return this;
	}

	public SpringProcessLiveDataBuilder contextPath(String contextPath) {
		this.contextPath = contextPath;
		return this;
	}

	public SpringProcessLiveDataBuilder contextPathEnvJson(String bootVersion, String envJson) {
		this.contextPath = LiveContextPathUtil.getContextPath(bootVersion, envJson);
		return this;
	}

	public SpringProcessLiveDataBuilder urlScheme(String urlScheme) {
		this.urlScheme = urlScheme;
		return this;
	}

	public SpringProcessLiveDataBuilder port(String port) {
		this.port = port;
		return this;
	}

	public SpringProcessLiveDataBuilder host(String host) {
		this.host = host;
		return this;
	}

	public SpringProcessLiveDataBuilder beans(LiveBeansModel beansModel) {
		this.beansModel = beansModel;
		return this;
	}

	public SpringProcessLiveDataBuilder activeProfiles(String... activeProfiles) {
		this.activeProfiles = activeProfiles;
		return this;
	}

	public SpringProcessLiveDataBuilder requestMappings(LiveRequestMapping... requestMappings) {
		this.requestMappings = requestMappings;
		return this;
	}
	
	public SpringProcessLiveDataBuilder requestMappingsJson(String json) {
		JSONObject obj = new JSONObject(json);

		List<LiveRequestMapping> result = new ArrayList<>();
		Iterator<String> keys = obj.keys();
		while (keys.hasNext()) {
			String rawKey = keys.next();
			JSONObject value = obj.getJSONObject(rawKey);
			result.add(new LiveRequestMappingBoot1xRequestMapping(rawKey, value));
		}
		this.requestMappings = result.toArray(new LiveRequestMapping[result.size()]);
		return this;
	}

	public SpringProcessLiveDataBuilder liveConditionals(LiveConditional[] conditionals) {
		this.conditionals = conditionals;
		return this;
	}

	public SpringProcessLiveDataBuilder liveConditionalsJson(String json) {
		this.conditionals = LiveConditionalParser.parse(json, processID, processName);
		return this;
	}

	public SpringProcessLiveDataBuilder getLiveProperties(LiveProperties properties) {
		this.properties = properties;
		return this;
	}
	
	public SpringProcessLiveData build() {
		return new SpringProcessLiveData(processName, processID, contextPath, urlScheme, port, host, beansModel, activeProfiles, requestMappings, conditionals, properties);
	}

}
