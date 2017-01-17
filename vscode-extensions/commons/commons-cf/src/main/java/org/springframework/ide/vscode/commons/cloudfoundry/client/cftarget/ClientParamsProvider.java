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

import java.util.List;

public interface ClientParamsProvider {

	/**
	 * 
	 * @return non-null list of params to connect to Cloud Foundry
	 * @throws Exception if failure to resolve any params for Cloud Foundry
	 */
	List<CFClientParams> getParams() throws Exception;

}