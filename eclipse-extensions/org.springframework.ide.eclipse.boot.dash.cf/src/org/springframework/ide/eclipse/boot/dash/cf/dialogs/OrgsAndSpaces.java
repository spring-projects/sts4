/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFSpace;

/**
 * Hierarchical representation of existing orgs and spaces in a Cloud Foundry
 * target.
 *
 */
public class OrgsAndSpaces {

	private final List<CFSpace> originalSpaces;
	private Map<String, List<CFSpace>> orgIDtoSpaces;
	private Map<String,CFOrganization> orgIDtoOrg;

	/**
	 *
	 * @param spaces
	 *            a flat list of all spaces for a given set of credentials and
	 *            server URL. Should not be empty or null.
	 * @throws CoreException
	 *             if given cloud server does not support orgs and spaces
	 */
	public OrgsAndSpaces(List<CFSpace> spaces) {
		this.originalSpaces = spaces;
		setValues();
	}

	public CFSpace getSpace(String orgName, String spaceName) {
		List<CFSpace> oSpaces = orgIDtoSpaces.get(orgName);
		if (oSpaces != null) {
			for (CFSpace clSpace : oSpaces) {
				if (clSpace.getName().equals(spaceName)) {
					return clSpace;
				}
			}
		}
		return null;
	}

	public List<CFOrganization> getOrgs() {

		Collection<CFOrganization> orgList = orgIDtoOrg.values();
		return new ArrayList<CFOrganization>(orgList);
	}

	protected void setValues() {
		orgIDtoSpaces = new HashMap<String, List<CFSpace>>();
		orgIDtoOrg = new HashMap<String, CFOrganization>();

		for (CFSpace clSpace : originalSpaces) {
			CFOrganization org = clSpace.getOrganization();
			List<CFSpace> spaces = orgIDtoSpaces.get(org.getName());
			if (spaces == null) {
				spaces = new ArrayList<CFSpace>();
				orgIDtoSpaces.put(org.getName(), spaces);
				orgIDtoOrg.put(org.getName(), org);
			}

			spaces.add(clSpace);
		}
	}

	/**
	 * @param orgName
	 * @return
	 */
	public List<CFSpace> getOrgSpaces(String orgName) {
		return orgIDtoSpaces.get(orgName);
	}

	/**
	 * @return all spaces available for the given account. Never null, although
	 *         may be empty if no spaces are resolved.
	 */
	public List<CFSpace> getAllSpaces() {
		return originalSpaces != null ? new ArrayList<CFSpace>(originalSpaces) : new ArrayList<CFSpace>(0);
	}
}
