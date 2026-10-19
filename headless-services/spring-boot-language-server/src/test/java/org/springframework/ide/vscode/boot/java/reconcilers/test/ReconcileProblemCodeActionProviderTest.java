/*******************************************************************************
 * Copyright (c) 2026 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.ReconcileProblemCodeActionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;

import com.google.gson.JsonParser;

/**
 * @author Martin Lippert
 */
public class ReconcileProblemCodeActionProviderTest {

	@Test
	void ignoredProblemFixesTurnedOffByFeatureFlag() {
		BootJavaConfig config = new BootJavaConfig(null);

		JdtAstReconciler reconciler = mock(JdtAstReconciler.class);
		when(reconciler.isApplicable(any())).thenReturn(true);

		JdtReconciler jdtReconciler = new JdtReconciler(null, config, new JdtAstReconciler[] { reconciler }, mock(ProjectObserver.class));
		ReconcileProblemCodeActionProvider provider = new ReconcileProblemCodeActionProvider(jdtReconciler, mock(DiagnosticSeverityProvider.class));
		IJavaProject project = mock(IJavaProject.class);

		assertTrue(provider.isApplicable(project));

		config.handleConfigurationChange(new Settings(JsonParser.parseString("{\"boot-java\": {\"feature-flags\": {\"ignored-problem-fixes\": false}}}")));
		assertFalse(provider.isApplicable(project));

		config.handleConfigurationChange(new Settings(JsonParser.parseString("{\"boot-java\": {\"feature-flags\": {\"ignored-problem-fixes\": true}}}")));
		assertTrue(provider.isApplicable(project));
	}

}
