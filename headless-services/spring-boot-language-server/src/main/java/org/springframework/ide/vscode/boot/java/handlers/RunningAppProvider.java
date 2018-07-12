/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collection;

import org.springframework.ide.vscode.commons.boot.app.cli.LocalSpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

import com.google.common.collect.ImmutableList;

public interface RunningAppProvider {

	static RunningAppProvider composite(RunningAppProvider... children) {
		if (children.length==1) {
			return children[0];
		}
		return () -> {
			ImmutableList.Builder<SpringBootApp> allApps = ImmutableList.builder();
			for (RunningAppProvider c : children) {
				Collection<SpringBootApp> moreApps = c.getAllRunningSpringApps();
				if (moreApps!=null) {
					allApps.addAll(moreApps);
				}
			}
			return allApps.build();
		};
	}

	public static final RunningAppProvider LOCAL_APPS = LocalSpringBootApp::getAllRunningSpringApps;

	public static final RunningAppProvider NULL = () -> ImmutableList.of();

	/**
	 * returns all running spring boot applications on the local machine.
	 */
	Collection<SpringBootApp> getAllRunningSpringApps() throws Exception;

	static RunningAppProvider createDefault(SimpleLanguageServer server) {
		return composite(LOCAL_APPS, new RemoteRunningAppsProvider(server));
	}
}
