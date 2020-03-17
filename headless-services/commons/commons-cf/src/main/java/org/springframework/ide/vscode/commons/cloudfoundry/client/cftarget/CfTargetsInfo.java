/*******************************************************************************
 * Copyright (c) 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.util.List;

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
	private TargetDiagnosticMessages diagnosticMessages;

	public List<Target> getCfTargets() {
		return cfTargets;
	}

	public void setCfTargets(List<Target> cfTargets) {
		this.cfTargets = cfTargets;
	}

	public TargetDiagnosticMessages getDiagnosticMessages() {
		return diagnosticMessages;
	}

	public void setDiagnosticMessages(TargetDiagnosticMessages diagnosticMessages) {
		this.diagnosticMessages = diagnosticMessages;
	}

	public static class TargetDiagnosticMessages {


		private String noTargetsFound;
		private String connectionError;
		private String noOrgSpace;
		private String targetSource;

		/**
		 * 
		 * @return only when there are no targets available (e.g. cf CLI is not connected or no boot dash targets)
		 */
		public String getNoTargetsFound() {
			return noTargetsFound;
		}

		public void setNoTargetsFound(String noTargetsFound) {
			this.noTargetsFound = noTargetsFound;
		}

		/**
		 * 
		 * @return error if any existing target cannot connect.
		 */
		public String getConnectionError() {
			return connectionError;
		}

		public void setConnectionError(String connectionError) {
			this.connectionError = connectionError;
		}

		public String getNoOrgSpace() {
			return noOrgSpace;
		}

		public void setNoOrgSpace(String noOrgSpace) {
			this.noOrgSpace = noOrgSpace;
		}

		public String getTargetSource() {
			return targetSource;
		}

		public void setTargetSource(String targetSource) {
			this.targetSource = targetSource;
		}
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

		@Override
		public String toString() {
			return "Target [api=" + api + ", org=" + org + ", space=" + space + "]";
		}
	}

	@Override
	public String toString() {
		return "CfTargetsInfo "+cfTargets;
	}
	
}
