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
package org.springframework.ide.vscode.commons.boot.app.cli.livebean;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Martin Lippert
 */
public class LiveBean {

	public static LiveBean parse(JSONObject beansJSON) {
		String id = beansJSON.optString("bean");
		String type = beansJSON.optString("type");
		String scope = beansJSON.optString("scope");
		String resource = beansJSON.optString("resource");

		JSONArray aliasesJSON = beansJSON.getJSONArray("aliases");
		String[] aliases = new String[aliasesJSON.length()];
		for (int i = 0; i < aliasesJSON.length(); i++) {
			aliases[i] = aliasesJSON.optString(i);
		}

		JSONArray dependenciesJSON = beansJSON.getJSONArray("dependencies");
		String[] dependencies = new String[dependenciesJSON.length()];
		for (int i = 0; i < dependenciesJSON.length(); i++) {
			dependencies[i] = dependenciesJSON.optString(i);
		}

		return new LiveBean(id, aliases, scope, type, resource, dependencies);
	}

	private final String id;
	private final String[] aliases;
	private final String scope;
	private final String type;
	private final String resource;
	private final String[] dependencies;

	protected LiveBean(String id, String[] aliases, String scope, String type, String resource, String[] dependencies) {
		super();
		this.id = id;
		this.aliases = aliases;
		this.scope = scope;
		this.type = type;
		this.resource = resource;
		this.dependencies = dependencies;
	}

	public String getId() {
		return id;
	}

	public String[] getAliases() {
		return aliases;
	}

	public String getScope() {
		return scope;
	}

	public String getType() {
		return type;
	}

	public String getResource() {
		return resource;
	}

	public String[] getDependencies() {
		return dependencies;
	}

}
