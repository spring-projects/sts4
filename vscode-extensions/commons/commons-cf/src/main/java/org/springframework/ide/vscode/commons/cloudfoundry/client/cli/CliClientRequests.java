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
package org.springframework.ide.vscode.commons.cloudfoundry.client.cli;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpackImpl;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstanceImpl;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientRequests;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CliClientRequests implements ClientRequests {

	public static final String RESOURCES = "resources";
	public static final String ENTITY = "entity";
	public static final String NAME = "name";

	@Override
	public List<CFBuildpack> getBuildpacks() throws Exception {
		List<CFBuildpack> buildpacks = new ArrayList<CFBuildpack>();
		String raw = runExternal("/usr/local/bin/cf", "curl", "/v2/buildpacks");
		Map<String, Object> jsonMap = jsonAsMap(raw);
		List<String> names = getEntityNames(jsonMap);
		for (String name : names) {
			CFBuildpackImpl buildpack = new CFBuildpackImpl(name);
			buildpacks.add(buildpack);
		}

		return buildpacks;
	}

	@Override
	public List<CFServiceInstance> getServices() throws Exception {
		String raw = runExternal("/usr/local/bin/cf", "curl", "/v2/service_instances");
		List<CFServiceInstance> services = new ArrayList<CFServiceInstance>();

		Map<String, Object> jsonMap = jsonAsMap(raw);
		List<String> names = getEntityNames(jsonMap);

		// The /service_instances endpoint only returns partial information. To
		// avoid another
		// CF call to fetch associated service information (e.g. the plan,
		// description, etc..)
		// just create a very basic service instance with minimal information
		// like the name

		for (String name : names) {
			CFServiceInstanceImpl instance = new CFServiceInstanceImpl(name);
			services.add(instance);
		}

		return services;
	}

	protected String runExternal(String... commands) throws Exception {

		File workingDir = null; // current working dir
		boolean captureStream = true;
		ExternalCommand cmd = asExternalCommand(commands);
		if (cmd == null) {
			return null;
		}
		try {
			ExternalProcess process = new ExternalProcess(workingDir, cmd, captureStream);
			String out = process.getOut();
			String error = process.getErr();

			if (StringUtil.hasText(out)) {
				return out;
			} else if (StringUtil.hasText(error)) {
				throw new Exception(error);
			}
		} catch (InterruptedException e) {
			throw ExceptionUtil.exception("Unable to complete cf CLI operation", e);
		}
		return null;
	}

	protected ExternalCommand asExternalCommand(String... commands) {
		return new ExternalCommand(commands);
	}

	/**
	 * 
	 * @param rawJson
	 * @return non-null Map. Empty if nothing parsed.
	 * @throws Exception
	 *             if error occurs during parsing
	 */
	protected Map<String, Object> jsonAsMap(String rawJson) throws Exception {
		if (rawJson != null) {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> data = mapper.readValue(new StringReader(rawJson), Map.class);
			return data;
		}
		return Collections.emptyMap();
	}

	protected List<Map<String, Object>> getEntities(Map<String, Object> fullMap) {
		List<?> resources = (List<?>) fullMap.get(RESOURCES);
		if (resources != null) {
			List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
			for (Object resource : resources) {
				if (resource instanceof Map<?, ?>) {
					Map<String, Object> entity = (Map<String, Object>) ((Map<?, ?>) resource).get(ENTITY);
					if (entity != null) {
						entities.add(entity);
					}
				}
			}
			return entities;
		}
		return Collections.emptyList();
	}

	protected List<String> getEntityNames(Map<String, Object> fullMap) {
		List<Map<String, Object>> entities = getEntities(fullMap);
		List<String> names = new ArrayList<>();
		for (Map<String, Object> map : entities) {
			String name = (String) map.get(NAME);
			if (name != null) {
				names.add(name);
			}
		}
		return names;
	}
}
