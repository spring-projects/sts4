/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfo.TargetDiagnosticMessages;

public interface ClientParamsProvider {
	
	/**
	 * 
	 * @return non-null list of VALID params to connect to Cloud Foundry
	 * @throws NoTargetsException if failure to resolve any params for Cloud Foundry
	 * @throws ExecutionException if failure occurs while resolving params
	 */
	Collection<CFClientParams> getParams() throws NoTargetsException, ExecutionException;
	
	TargetDiagnosticMessages getMessages();

	
}