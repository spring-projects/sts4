/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.links;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.lsp4j.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMember;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;

public class JavaServerElementLocationProvider implements JavaElementLocationProvider {

	private static final Logger log = LoggerFactory.getLogger(JavaServerElementLocationProvider.class);

	private SimpleLanguageServer server;

	public JavaServerElementLocationProvider(SimpleLanguageServer server) {
		this.server = server;
	}

	@Override
	public Location findLocation(IJavaProject project, IMember member) {
		String projectUri = project == null ? null : project.getLocationUri().toString();
		String bindingKey = member.getBindingKey();
		try {
			Location location = server.getClient().javaLocation(new JavaDataParams(projectUri, bindingKey, true)).get(500, TimeUnit.MILLISECONDS);
			return location;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("", e);
			return null;
		}
	}

}
