/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.io.File;
import java.util.List;

import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProject;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;

import com.google.common.collect.ImmutableList;

public class SpringProjectsValidations {

	private final SpringProjectsCache cache;

	public SpringProjectsValidations(SpringProjectsCache cache) {
		this.cache = cache;
	}

	public List<String> getVersionWarnings(IJavaProject jp) throws Exception {
		ImmutableList.Builder<String> messages = ImmutableList.builder();

		if (jp != null) {
			List<File> librariesOnClasspath = SpringProjectUtil.getLibrariesOnClasspath(jp, "spring");
			if (librariesOnClasspath != null) {
				for (File file : librariesOnClasspath) {
					String fileName = file.getName();
					String slug = SpringProjectUtil.getProjectSlug(fileName);
					String majMin = SpringProjectUtil.getMajMinVersion(fileName);
					String fullVersion = SpringProjectUtil.getVersion(fileName);
					SpringProject springProject = cache.getProject(slug);
					if (springProject != null && majMin != null) {
						Generations generations = cache.getGenerations(springProject);
						if (generations != null) {
							List<Generation> gens = generations.getGenerations();
							if (gens != null) {
								Generation gen = null;
								for (Generation g : gens) {
									if (isInGeneration(majMin, g)) {
										gen = g;
										break;
									}
								}
								if (gen != null) {
									messages.add(getWarning(slug, fullVersion, gen));
								}
							}
						}
					}
				}
			}
		}
		return messages.build();
	}

	private String getWarning(String slug, String version, Generation gen) {
		StringBuilder msg = new StringBuilder();
		msg.append("Using ");
		msg.append(slug);
		msg.append(" version: ");
		msg.append(version);
		msg.append(" - OSS support end date: ");
		msg.append(gen.getOssSupportEndDate());
		return msg.toString();
	}

	private boolean isInGeneration(String version, Generation g) {
		return g.getName().contains(version);
	}
}
