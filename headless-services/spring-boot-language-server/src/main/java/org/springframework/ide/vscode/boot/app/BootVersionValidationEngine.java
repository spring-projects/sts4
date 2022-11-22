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
package org.springframework.ide.vscode.boot.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.stereotype.Component;

@Component
public class BootVersionValidationEngine {

	private final BootVersionValidator bootVersionValidator;
	private final ExecutorService validationExecutor = Executors.newFixedThreadPool(3);
	
	public BootVersionValidationEngine(ProjectObserver observer, BootVersionValidator bootVersionValidator) {
		this.bootVersionValidator = bootVersionValidator;
		
		observer.addListener(new ProjectObserver.Listener() {

			@Override
			public void deleted(IJavaProject project) {
			}

			@Override
			public void created(IJavaProject project) {
				validate(project);
			}

			@Override
			public void changed(IJavaProject project) {

			}
		});
	}
	
	public void validate(IJavaProject project) {
		validationExecutor.submit(() -> bootVersionValidator.validate(project));
	}

}
