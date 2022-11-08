/*******************************************************************************
 * Copyright (c) 2020, 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.util.List;

import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.commons.java.IJavaProject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class SpringBootProjectValidations {

	private final SpringProjectsProvider projectsProvider;
	private static final String SPRING_BOOT_PROJECT_SLUG = "spring-boot";
	
	private final BootDiagnosticProvider[] diagnosticProviders = new BootDiagnosticProvider[] {
			new UnsupportedVersionDiagnostic()
	};

	public SpringBootProjectValidations(SpringProjectsProvider projectsProvider) {
		this.projectsProvider = projectsProvider;
	}
	
	public List<SpringProjectDiagnostic> validateBootVersion(IJavaProject project) throws Exception {
		Builder<SpringProjectDiagnostic> builder = ImmutableList.builder();
		if (project != null) {
			SpringDependencyInfo info = new SpringDependencyInfo(project, SPRING_BOOT_PROJECT_SLUG);
			Generations generations = projectsProvider.getGenerations(SPRING_BOOT_PROJECT_SLUG);
			
			for (BootDiagnosticProvider provider : diagnosticProviders) {
				SpringProjectDiagnostic diagnostic = provider.getDiagnostic(project, info, generations);
				if (diagnostic != null) {
					builder.add(diagnostic);
				}
			}
		}
		return builder.build();
	}
	
}
