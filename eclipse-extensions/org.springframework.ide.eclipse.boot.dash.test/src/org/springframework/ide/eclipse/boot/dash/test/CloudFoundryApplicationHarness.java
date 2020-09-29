/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.cf.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.dash.console.IApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.console.LogMessage;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import reactor.core.Disposable;

public class CloudFoundryApplicationHarness extends AbstractDisposable {

	private Set<String> ownedAppNames  = new HashSet<>();
	private DefaultClientRequestsV2 client;

	public CloudFoundryApplicationHarness(DefaultClientRequestsV2 client) {
		this.client = client;
		if (client!=null) {
			onDispose((d) -> { deleteOwnedApps(); });
		}
	}

	public String randomAppName() throws Exception {
		String name = StringUtil.datestamp()+"-"+randomAlphabetic(10);
		ownedAppNames.add(name);
		streamOutput(name);
		return name;
	}

	private void streamOutput(String name) throws Exception {
		if (client!=null) {
			IApplicationLogConsole logConsole = new IApplicationLogConsole() {

				@Override
				public void onMessage(LogMessage log) {
					System.out.println("%"+name+"-out: "+log.getMessage());
				}

				@Override
				public void onComplete() {
					System.out.println("%"+name+"-COMPLETE");
				}

				@Override
				public void onError(Throwable exception) {
					System.out.println("%"+name+"-ERROR: "+ExceptionUtil.getMessage(exception));
				}

			};
			Disposable logToken = client.streamLogs(name, logConsole);
			onDispose((d) -> {
				logToken.dispose();
			});
		}
	}

	protected void deleteOwnedApps() {
		System.out.println("owned app names: "+ownedAppNames);
		if (!ownedAppNames.isEmpty()) {

			try {
				for (String name : ownedAppNames) {
					try {
						System.out.println("delete owned app: "+name);
						this.client.deleteApplication(name);
					} catch (Exception e) {
						System.out.println("Delete failed: " +ExceptionUtil.getMessage(e));
						// May get 404 or other 400 errors if it is alrready
						// gone so don't prevent other owned apps from being
						// deleted
					}
				}

			} catch (Exception e) {
				fail("failed to cleanup owned apps: " + e.getMessage());
			}
		}
	}

}
