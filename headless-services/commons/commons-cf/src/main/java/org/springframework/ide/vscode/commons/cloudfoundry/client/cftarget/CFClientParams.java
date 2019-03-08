/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFCredentials.CFCredentialType;
import org.springframework.ide.vscode.commons.util.Assert;

/**
 * All the parameters needed to create a CF client.
 *
 * @author Kris De Volder
 */
public class CFClientParams {

	private static final Logger logger = Logger.getLogger(CFClientParams.class.getName());

	private final String apiUrl;
	private final String username;
	private CFCredentials credentials;
	private final boolean skipSslValidation;

	private String orgName; // optional
	private String spaceName; // optional

	public CFClientParams(String apiUrl,
			String username,
			CFCredentials credentials,
			String orgName,
			String spaceName,
			boolean skipSslValidation
	) {
		Assert.isNotNull(apiUrl);
		Assert.isNotNull(credentials);
		if (credentials.getType() == CFCredentialType.PASSWORD) {
			Assert.isNotNull(username);
		}
		this.apiUrl = apiUrl;
		this.username = username;
		this.credentials = credentials;
		this.skipSslValidation = skipSslValidation;
		this.orgName = orgName;
		this.spaceName = spaceName;
	}

	public CFClientParams(String apiUrl, String username, CFCredentials credentials, boolean skipSslValidation) {
		this(apiUrl, username, credentials, null /* no org */,
				null /* no space */, skipSslValidation);
	}

	public CFCredentials getCredentials() {
		return credentials;
	}

	public String getUsername() {
		return username;
	}

	public boolean skipSslValidation() {
		return skipSslValidation;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getSpaceName() {
		return spaceName;
	}

	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	public String getHost() {
		try {
			URI uri = new URI(getApiUrl());
			return uri.getHost();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);

		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((apiUrl == null) ? 0 : apiUrl.hashCode());
		result = prime * result + ((credentials == null) ? 0 : credentials.hashCode());
		result = prime * result + ((orgName == null) ? 0 : orgName.hashCode());
		result = prime * result + (skipSslValidation ? 1231 : 1237);
		result = prime * result + ((spaceName == null) ? 0 : spaceName.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CFClientParams other = (CFClientParams) obj;
		if (apiUrl == null) {
			if (other.apiUrl != null)
				return false;
		} else if (!apiUrl.equals(other.apiUrl))
			return false;
		if (credentials == null) {
			if (other.credentials != null)
				return false;
		} else if (!credentials.equals(other.credentials))
			return false;
		if (orgName == null) {
			if (other.orgName != null)
				return false;
		} else if (!orgName.equals(other.orgName))
			return false;
		if (skipSslValidation != other.skipSslValidation)
			return false;
		if (spaceName == null) {
			if (other.spaceName != null)
				return false;
		} else if (!spaceName.equals(other.spaceName))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CFClientParams [apiUrl=" + apiUrl + ", username=" + username + ", credentials=" + credentials
				+ ", skipSslValidation=" + skipSslValidation + ", orgName=" + orgName + ", spaceName=" + spaceName
				+ "]";
	}

}
