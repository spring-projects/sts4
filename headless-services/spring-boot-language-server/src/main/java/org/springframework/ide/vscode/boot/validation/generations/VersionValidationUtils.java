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

	public static Version getLatestSupportedRelease(ResolvedSpringProject springProject)
			throws Exception {
		List<Version> rls = springProject.getReleases();
		return rls.isEmpty() ? null : rls.get(rls.size() - 1);
	}
	
	public static Version getNewestPatchRelease(List<Version> releases, Version version) {
		Version result = version;
		
		for (Version release : releases) {
			if (release.getMajor() == result.getMajor()
					&& release.getMinor() == result.getMinor()
					&& release.getPatch() > result.getPatch()) {
				result = release;
			}
		}
		
		return result;
	}

	public static Version getNewestMinorRelease(List<Version> releases, Version version) {
		Version result = version;
		
		for (Version release : releases) {
			if (release.getMajor() == result.getMajor()
					&& (release.getMinor() > result.getMinor()
					|| (release.getMinor() == result.getMinor() && release.getPatch() > result.getPatch()))) {
				result = release;
			}
		}
		
		return result;
	}

	public static Version getNewestMajorRelease(List<Version> releases, Version version) {
		Version result = version;
		
		for (Version release : releases) {
			if (release.getMajor() > result.getMajor()
					|| (release.getMajor() == result.getMajor() && release.getMinor() > result.getMinor())
					|| (release.getMajor() == result.getMajor() && release.getMinor() == result.getMinor() && release.getPatch() > result.getPatch())) {
				result = release;
			}
		}
		
		return result;
	}
	
	public static Version getNewerLatestPatchRelease(List<Version> releases, Version version) {
		Version newestPatch = getNewestPatchRelease(releases, version);
		return newestPatch.equals(version) ? null : newestPatch;
	}

	public static Version getNewerLatestMinorRelease(List<Version> releases, Version version) {
		Version newestMinor = getNewestMinorRelease(releases, version);
		return newestMinor.getMinor() == version.getMinor() ? null : newestMinor;
	}

	public static Version getNewerLatestMajorRelease(List<Version> releases, Version version) {
		Version newestMajor = getNewestMajorRelease(releases, version);
		return newestMajor.getMajor() == version.getMajor() ? null : newestMajor;
	}

}
