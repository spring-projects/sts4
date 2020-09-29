/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import java.util.List;

import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFDomainStatus;

import com.google.common.collect.ImmutableList;

public class CloudData {

	private List<CFCloudDomain> domains;
	private String buildpack;
	private List<CFStack> stacks;

	public CloudData(List<CFCloudDomain> domains, String buildpack, List<CFStack> stacks) {
		this.domains = domains;
		this.buildpack = buildpack;
		this.stacks = stacks;
	}

	public String getBuildpack() {
		return buildpack;
	}

	public List<CFCloudDomain> getDomains() {
		if (domains!=null) {
			return domains;
		}
		return ImmutableList.of();
	}

	public List<CFStack> getStacks() {
		if (stacks!=null) {
			return stacks;
		}
		return ImmutableList.of();
	}

	public String getDefaultDomain() {
		return getDomains().stream()
				.filter(d -> d.getStatus()==CFDomainStatus.SHARED && d.getType()==CFDomainType.HTTP)
				.findFirst()
				.map(d -> d.getName())
				.orElse(null);
	}

}
