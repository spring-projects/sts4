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
package org.springframework.ide.vscode.commons.util;

import java.io.File;
import java.io.IOException;

public class LogRedirect {

	public static void redirectToFile(String name) throws IOException {
		File logfile = null;
		if (System.getProperty("org.slf4j.simpleLogger.logFile") == null) {
			logfile = File.createTempFile(name, ".log");
			System.setProperty("org.slf4j.simpleLogger.logFile", logfile.toString());
		} else {
			logfile = new File(System.getProperty("org.slf4j.simpleLogger.logFile"));
		}
		System.err.println("Redirecting log output to: "+logfile);
	}
	
}
