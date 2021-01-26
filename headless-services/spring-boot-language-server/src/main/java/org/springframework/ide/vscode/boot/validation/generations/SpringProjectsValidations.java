/*******************************************************************************
 * Copyright (c) 2020, 2021 Pivotal, Inc.
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
import java.sql.Date;
import java.util.List;

import org.eclipse.lsp4j.MessageType;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

public class SpringProjectsValidations {

	private final List<SpringProjectsProvider> projectsProviders;
	private final SimpleLanguageServer server;

	public SpringProjectsValidations(SimpleLanguageServer server, List<SpringProjectsProvider> projectsProviders) {
		this.projectsProviders = projectsProviders;
		this.server = server;
	}
	
	public ProjectValidation validateVersion(IJavaProject jp) throws Exception {
		StringBuilder builder = new StringBuilder();
		if (jp != null) {
			List<File> librariesOnClasspath = SpringProjectUtil.getLibrariesOnClasspath(jp, "spring");
			if (librariesOnClasspath != null) {
				for (File file : librariesOnClasspath) {
					SpringVersionInfo versionInfo = new SpringVersionInfo(file);
					for (SpringProjectsProvider projectsProvider : projectsProviders) {
						Generations generations = projectsProvider.getGenerations(versionInfo.getSlug());
						if (generations != null) {
							List<Generation> gens = generations.getGenerations();
							if (gens != null) {
								for (Generation gen : gens) {
									resolveWarnings(gen, builder, versionInfo);
								}
							}
						}
					}
				}
			}
		}
		
		return builder.length() > 0 ? 
				new ProjectValidation(builder.toString(), MessageType.Warning)
				: ProjectValidation.OK;
	}

	private void resolveWarnings(Generation gen, StringBuilder messages, SpringVersionInfo versionInfo) {
		if (isInGeneration(versionInfo.getMajMin(), gen)) {
			Date currentDate = new Date(System.currentTimeMillis());
			Date ossEndDate =  Date.valueOf(gen.getOssSupportEndDate());
			Date commercialEndDate = Date.valueOf(gen.getCommercialSupportEndDate());
						
			messages.append("Using ");
			messages.append(versionInfo.getSlug());
			messages.append(" version: ");
			messages.append(versionInfo.getFullVersion());
			
			if (currentDate.after(ossEndDate)) {
				messages.append(" - OSS has ended on: ");
				messages.append(gen.getOssSupportEndDate());
			}
			if (currentDate.after(commercialEndDate)) {
				messages.append(" - Commercial support has ended on: ");
				messages.append(gen.getCommercialSupportEndDate());
			}
		}
	}
	
	private boolean isInGeneration(String version, Generation g) {
		return g.getName().contains(version);
	}
}
