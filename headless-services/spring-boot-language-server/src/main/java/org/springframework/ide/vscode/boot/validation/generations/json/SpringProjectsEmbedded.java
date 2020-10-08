/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations.json;

public class SpringProjectsEmbedded extends JsonHalLinks implements JsonHalEmbedded<SpringProjects> {

	private SpringProjects _embedded;

	public SpringProjects get_embedded() {
		return _embedded;
	}

	public void set_embedded(SpringProjects _embedded) {
		this._embedded = _embedded;
	}
}
