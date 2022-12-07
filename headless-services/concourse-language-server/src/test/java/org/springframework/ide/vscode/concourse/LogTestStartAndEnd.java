/*******************************************************************************
 * Copyright (c) 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.concourse;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTestStartAndEnd implements Extension, BeforeEachCallback, AfterEachCallback {
		
	private static final Logger log = LoggerFactory.getLogger(LogTestStartAndEnd.class);
	
	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		log.info("<<<< finished test: "+context.getTestClass().map(c -> c.getName()).orElseThrow()+" . "+context.getTestMethod().orElseThrow());
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		log.info(">>>> starting test: "+context.getTestClass().map(c -> c.getName()).orElseThrow()+" . "+context.getTestMethod().orElseThrow());
	}
}
