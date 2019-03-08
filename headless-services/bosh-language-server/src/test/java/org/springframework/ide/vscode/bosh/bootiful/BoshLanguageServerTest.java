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
package org.springframework.ide.vscode.bosh.bootiful;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ide.vscode.bosh.BoshLanguageServerBootApp;
import org.springframework.ide.vscode.languageserver.starter.LanguageServerAutoConf;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@Retention(RetentionPolicy.RUNTIME)
@OverrideAutoConfiguration(enabled=false)
@ImportAutoConfiguration(classes=LanguageServerAutoConf.class)
@SpringBootTest(classes={
		BoshLanguageServerBootApp.class,
		BoshLanguageServerTestConfiguration.class
})
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public @interface BoshLanguageServerTest {
}
