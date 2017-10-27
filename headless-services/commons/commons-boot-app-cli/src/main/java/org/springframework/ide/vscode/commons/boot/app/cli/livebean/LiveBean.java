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
 * @author Kris De Volder
 */
public class LiveBean {

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
