/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON metadata about reference apps retrieved from some external url.
 *
 * @author Kris De Volder
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceAppMetaData {

	@JsonProperty("name")
	private String name; //optional. If not provided reference app name will be repo name

	@JsonProperty("type")
	private String type; //only legal value now is "github". In the future maybe we will allow other types of metadata
						  // to define to other ways/places of obtaining the sample code.

	@JsonProperty("description")
	private String description; //optional if not provided will use the github repo's description

	@JsonProperty("owner")
	private String owner; //mandatory: github repo owner name

	@JsonProperty("repo")
	private String repo; ///mandatory:  repo name

	@JsonProperty("branch")
	private String branch; ///optional:  branch name (if not set, we'll assume 'master')

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	public String getOwner() {
		return owner;
	}
	public String getRepo() {
		return repo;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

}
