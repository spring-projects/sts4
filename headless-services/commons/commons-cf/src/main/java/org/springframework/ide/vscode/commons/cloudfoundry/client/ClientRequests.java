/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client;


import java.util.List;

public interface ClientRequests {

	List<CFBuildpack> getBuildpacks() throws Exception;
	List<CFServiceInstance> getServices() throws Exception;
	List<CFDomain> getDomains() throws Exception;
	List<CFStack> getStacks() throws Exception;

}
