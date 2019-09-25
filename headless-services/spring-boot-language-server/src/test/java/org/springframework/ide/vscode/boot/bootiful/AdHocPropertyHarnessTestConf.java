/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.bootiful;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.editor.harness.AdHocPropertyHarness;
import org.springframework.ide.vscode.boot.metadata.ProjectBasedPropertyIndexProvider;

@Configuration
public class AdHocPropertyHarnessTestConf {
	@Bean AdHocPropertyHarness adHocPropertyHarness() {
		return new AdHocPropertyHarness();
	}

	@Bean ProjectBasedPropertyIndexProvider adHocProperties(AdHocPropertyHarness adHocProperties) {
		return adHocProperties.getIndexProvider();
	}
}
