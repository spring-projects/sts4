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

import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientRequests;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;

public class CliClientFactory implements CloudFoundryClientFactory {

	@Override
	public ClientRequests getClient(CFClientParams params, ClientTimeouts timeouts) throws Exception {
		// Doesn't really need params at this time. The CLI is external to
		// vscode,
		// and for now it is assumed to either already be connected or will
		// throw some error via calls to the request if it is not connected
		return new CliClientRequests();
	}

}
