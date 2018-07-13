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
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.util.Log;

/**
 * @author Martin Lippert
 */
public class LiveAppURLSymbolProvider {

	private final RunningAppProvider runningAppProvider;

	public LiveAppURLSymbolProvider(RunningAppProvider runningAppProvider) {
		this.runningAppProvider = runningAppProvider;
	}

	public List<? extends SymbolInformation> getSymbols(String query) {
		System.out.println(query);

		List<SymbolInformation> result = new ArrayList<>();

		try {
			SpringBootApp[] runningApps = runningAppProvider.getAllRunningSpringApps().toArray(new SpringBootApp[0]);
			for (SpringBootApp app : runningApps) {
				try {
					String host = app.getHost();
					String port = app.getPort();
					Stream<String> urls = app.getRequestMappings().stream()
							.flatMap(rm -> Arrays.stream(rm.getSplitPath()))
							.map(path -> UrlUtil.createUrl(host, port, path));
					urls.forEach(url -> result.add(new SymbolInformation(url, SymbolKind.Method, new Location(url, new Range(new Position(0, 0), new Position(0, 1))))));
				}
				catch (Exception e) {
					Log.log(e);
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}

		return result;
	}

}
