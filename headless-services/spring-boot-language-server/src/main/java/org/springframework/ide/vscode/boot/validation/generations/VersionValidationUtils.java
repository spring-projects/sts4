/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.commons.java.Version;

public class VersionValidationUtils {
	
	public static boolean isOssValid(Generation gen) {
		Date currentDate = new Date(System.currentTimeMillis());
		Date ossEndDate = Date.valueOf(gen.getOssSupportEndDate());
		return currentDate.before(ossEndDate);
	}

	public static boolean isCommercialValid(Generation gen) {
		Date currentDate = new Date(System.currentTimeMillis());
		Date commercialEndDate = Date.valueOf(gen.getCommercialSupportEndDate());
		return currentDate.before(commercialEndDate);
	}

	public static Version getNewerPatchVersion(ResolvedSpringProject springProject, Version version) throws Exception {
		Version result = null;
		List<Version> releases = springProject.getReleases();
		int found = Collections.binarySearch(releases, version);
		int index = found < 0 ? -found - 1 : found + 1;
		for (int i = index; i < releases.size() && releases.get(i).getMajor() == version.getMajor() && releases.get(i).getMinor() == version.getMinor(); i++) {
			result = releases.get(i);
		}
		return result;
	}

	public static Version getNewerMinorVersion(ResolvedSpringProject springProject, Version version) throws Exception {
		return getNewerPatchVersion(springProject, new Version(version.getMajor(), version.getMinor() + 1, 0, null));
	}

	public static Version getNewerMajorVersion(ResolvedSpringProject springProject, Version version) throws Exception {
		return getNewerPatchVersion(springProject, new Version(version.getMajor() + 1, 0, 0, null));
	}

	public static Version getLatestSupportedRelease(ResolvedSpringProject springProject)
			throws Exception {
		List<Version> rls = springProject.getReleases();
		return rls.isEmpty() ? null : rls.get(rls.size() - 1);
	}

}
