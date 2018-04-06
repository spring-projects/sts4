/*******************************************************************************
 * Copyright (c) 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.util.List;
import java.util.Map;

/**
 *  JSON-friendly representation of Cloud Foundry targets, used for integrations that need
 *  to serialise CF target information for transmission (e.g. between a client and server)
 *  <p/>
 *  WARNING: As this is used to serialise and deserialise between client and server, this exact type is used on both client and server. 
 *  Therefore making changes to this class requires changes in the client side as well. For example, see the identical copy of this class in:
 *  org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfTargetsInfo
 *
 */
public class CfTargetsInfo {

	private List<Target> cfTargets;
	private Map<String, String> cfDiagnosticMessages;

	public List<Target> getCfTargets() {
		return cfTargets;
	}

	public void setCfTargets(List<Target> cfTargets) {
		this.cfTargets = cfTargets;
	}

	public Map<String, String> getCfDiagnosticMessages() {
		return this.cfDiagnosticMessages;
	}

	public void setCfDiagnosticMessages(Map<String, String> cfDiagnosticMessages) {
		this.cfDiagnosticMessages = cfDiagnosticMessages;
	}

	public static class Target {
		private String api;
		private String org;
		private String space;

		private boolean sslDisabled;
		private String refreshToken;

		public String getApi() {
			return api;
		}

		public void setApi(String api) {
			this.api = api;
		}

		public String getOrg() {
			return org;
		}

		public void setOrg(String org) {
			this.org = org;
		}
	
		public void setSpace(String space) {
			this.space = space;
		}

		public String getSpace() {
			return space;
		}

		public boolean getSslDisabled() {
			return sslDisabled;
		}

		public void setSslDisabled(boolean sslDisabled) {
			this.sslDisabled = sslDisabled;
		}

		public String getRefreshToken() {
			return refreshToken;
		}

		public void setRefreshToken(String refreshToken) {
			this.refreshToken = refreshToken;
		}
	}
}
