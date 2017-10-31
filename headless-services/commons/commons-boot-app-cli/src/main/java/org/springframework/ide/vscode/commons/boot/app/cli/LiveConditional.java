/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;

public class LiveConditional {

	public final String condition;
	public final String message;
	public final SpringBootApp app;

	public LiveConditional(SpringBootApp app, String condition, String message) {
		this.condition = condition;
		this.message = message;
		this.app = app;
	}
}