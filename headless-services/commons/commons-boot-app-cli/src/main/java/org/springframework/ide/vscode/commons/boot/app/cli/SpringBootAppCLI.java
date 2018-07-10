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

import java.util.Collection;

/**
 * @author Martin Lippert
 */
public class SpringBootAppCLI {

	public static void main(String[] args) throws Exception {
		Collection<SpringBootApp> allRunningJavaApps = LocalSpringBootApp.getAllRunningJavaApps();
		for (SpringBootApp app : allRunningJavaApps) {
			if (app.isSpringBootApp()) {
				printBootAppDetails(app);
			}
		}
	}

	private static void printBootAppDetails(SpringBootApp app) throws Exception {
		System.out.println("Spring Boot App: " + app.getProcessID());
		System.out.println("Name: " + app.getProcessName());
		System.out.println("Port: " + app.getPort());
		System.out.println("Beans: " + app.getBeans());
		System.out.println("Mappings: " + app.getRequestMappings());
		System.out.println("ConfigReport: " + app.getLiveConditionals());
		System.out.println();
	}

}
