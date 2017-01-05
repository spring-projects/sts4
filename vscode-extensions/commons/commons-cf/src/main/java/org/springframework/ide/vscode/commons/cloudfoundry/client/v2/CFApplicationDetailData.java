/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import java.util.List;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFInstanceStats;

public class CFApplicationDetailData extends CFApplicationSummaryData implements CFApplicationDetail {

	private List<CFInstanceStats> instanceDetails;

	public CFApplicationDetailData(
			CFApplicationSummaryData app,
			List<CFInstanceStats> instanceDetails
	) {
		super(
				app.getName(),
				app.getInstances(),
				app.getRunningInstances(),
				app.getMemory(),
				app.getGuid(),
				app.getUris(),
				app.getState(),
				app.getDiskQuota(),
				app.extras
		);
		this.instanceDetails = instanceDetails;
	}

	@Override
	public List<CFInstanceStats> getInstanceDetails() {
		return instanceDetails;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("CFApplicationDetail(\n");
		buf.append("  name = "+getName()+"\n");
		buf.append("  instance = "+getInstances()+"\n");
		buf.append("  runningInstances = "+getRunningInstances()+"\n");
		buf.append("  memory = "+getMemory()+"\n");
		buf.append("  guid = "+getGuid()+"\n");
		buf.append("  uris = "+getUris()+"\n");
		buf.append("  state = "+getState()+"\n");
		buf.append("  diskQuota = "+getDiskQuota()+"\n");
		buf.append("  instanceDetails = "+getInstanceDetails()+"\n");
		buf.append(")\n");
		return buf.toString();
	}

}
