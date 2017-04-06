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
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

public final class CfCliProviderMessages implements CFParamsProviderMessages {

	/*
	 * Important: be sure to use ':' to separate the initial part of the message
	 * with a longer portion. The vscode content assist will parse around the
	 * first ':' and the second segment will appear as a doc string that can be
	 * longer
	 */
	public static final String NO_CLI_TARGETS_FOUND_MESSAGE = "No Cloud Foundry targets found: Use cf CLI to login";
	public static final String NO_NETWORK_CONNECTION = "No connection to Cloud Foundry: Use cf CLI to login or verify network connection";
	public static final String NO_ORG_SPACE = "No org/space selected: Use CF CLI to login";

	@Override
	public String noTargetsFound() {
		return NO_CLI_TARGETS_FOUND_MESSAGE;
	}

	@Override
	public String unauthorised() {
		return NO_NETWORK_CONNECTION;
	}

	@Override
	public String noNetworkConnection() {
		return NO_NETWORK_CONNECTION;
	}

	@Override
	public String noOrgSpace() {
		return NO_ORG_SPACE;
	}
}