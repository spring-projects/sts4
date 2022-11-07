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
package org.springframework.ide.vscode.boot.app;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.stereotype.Component;

@Component
public class ProblemSeverityConfigurer implements InitializingBean {

	@Autowired
	private SimpleLanguageServer server;

	@Autowired
	private BootJavaConfig config;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		config.addListener((x) -> configChanged());
		configChanged();
	}

	private void configChanged() {
		Settings settings = config.getRawSettings();
		settings = settings.navigate("spring-boot", "ls", "problem");
		
		Map<String, ProblemSeverity> severityOverrides = new HashMap<>();
		for (String editorType : settings.keys()) {
			Settings problemConf = settings.navigate(editorType);
			for (String code : problemConf.keys()) {
				String severity = problemConf.getString(code);
				Assert.isLegal(!severityOverrides.containsKey(code), "Multpile entries for problem type "+code);
				severityOverrides.put(code, ProblemSeverity.valueOf(severity));
			}
		}
		server.setDiagnosticSeverityProvider((ProblemType problem) -> {
			ProblemSeverity severity = severityOverrides.get(problem.getCode());
			if (severity==null) {
				severity = problem.getDefaultSeverity();
			}
			return DiagnosticSeverityProvider.diagnosticSeverity(severity);
		});
	}
}
