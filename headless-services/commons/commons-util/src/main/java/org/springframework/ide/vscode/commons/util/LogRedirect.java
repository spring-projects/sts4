/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class LogRedirect {

	public static void bootRedirectToFile(String name) throws IOException {
		String logfilePath = System.getProperty("sts.log.file");
		if (StringUtil.hasText(logfilePath)) {
			PrintStream logFile = logFileStream(logfilePath);
			System.setErr(logFile);
			System.setOut(logFile); //Spring boot actually logs on sysout instead of syserr.
		}
	}

	private static PrintStream logFileStream(String logfilePath) throws FileNotFoundException {
		if (logfilePath.equals("/dev/null")) {
			System.err.println("Disabling server log output. No more output will be sent after this.");
			//redirect to a file called "/dev/null" works fine in Unix, but we also want this
			// to work on Mac and Windows. So we create our own '/dev/null' stream
			return new PrintStream(new NullOutputStream());
		} else {
			System.err.println("Redirecting log output to: "+logfilePath);
			File logfile = new File(logfilePath);
			PrintStream logFile = new PrintStream(new FileOutputStream(logfile, false));
			return logFile;
		}
	}

	public static void redirectToFile(String name) throws IOException {
		String logfilePath = System.getProperty("sts.log.file");
		if (StringUtil.hasText(logfilePath)) {
			PrintStream logFile = logFileStream(logfilePath);
			System.setErr(logFile);
		}
	}
	
}
