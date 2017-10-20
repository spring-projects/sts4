/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.concourse;

import java.io.IOException;

import org.springframework.ide.vscode.commons.languageserver.LaunguageServerApp;
import org.springframework.ide.vscode.commons.yaml.completion.YamlCompletionEngineOptions;

public class Main {
	private static final YamlCompletionEngineOptions OPTIONS = YamlCompletionEngineOptions.DEFAULT;

   	public static void main(String[] args) throws IOException, InterruptedException {
		LaunguageServerApp.start("concourse", () -> new ConcourseLanguageServer(OPTIONS));
	}
}
