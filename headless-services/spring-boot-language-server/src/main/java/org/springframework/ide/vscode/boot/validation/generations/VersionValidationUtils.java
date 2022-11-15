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
import org.springframework.ide.vscode.boot.validation.generations.json.Release;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.commons.java.Version;

public class VersionValidationUtils {
	
	private static final String GENERAL_AVAILABILITY_STATUS = "GENERAL_AVAILABILITY";

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

	public static Version getLatestSupportedInSameMajor(ResolvedSpringProject springProject, Version version)
			throws Exception {
		List<Release> rls = springProject.getReleases();
		for (Release release : rls) {
			Version rlVersion = release.getVersion();
			if (GENERAL_AVAILABILITY_STATUS.equals(release.getStatus())
					&& rlVersion.getMajor() == version.getMajor()) {
				return rlVersion;
			}
		}
		return null;
	}

	public static Version getLatestSupportedRelease(ResolvedSpringProject springProject)
			throws Exception {
		List<Release> rls = springProject.getReleases();
		for (Release release : rls) {
			if (release.isCurrent()) {
				return release.getVersion();
			}
		}
		return null;
	}

}
