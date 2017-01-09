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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.ClientRequests;

public class CFTarget {

	private final CFClientParams params;
	private final ClientRequests requests;
	private final String targetName;

	/*
	 * Cached information
	 */
	private List<CFBuildpack> buildpacks;
	private final static Logger logger = Logger.getLogger(CFTarget.class.getName());

	public CFTarget(CFClientParams params, ClientRequests requests, String targetName) {
		this.params = params;
		this.requests = requests;
		this.targetName = targetName;
	}
	
	public CFClientParams getParams() {
		return params;
	}


	public List<CFBuildpack> getBuildpacks() {
		if (buildpacks == null) {
			try {
				buildpacks=getClientRequests().getBuildpacks();
			} catch (Exception e) {
				logger .log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return buildpacks;
	}

	public ClientRequests getClientRequests() {
		return requests;
	}

	public String getName() {
		return this.targetName;
	}

	@Override
	public String toString() {
		return "CFClientTarget [params=" + params + ", targetName=" + targetName + "]";
	}
	
	
}
