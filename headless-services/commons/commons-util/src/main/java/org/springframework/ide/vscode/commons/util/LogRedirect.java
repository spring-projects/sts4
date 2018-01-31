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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class LogRedirect {

	public static void redirectToFile(String name) throws IOException {
		String logfilePath = System.getProperty("sts.log.file");
		if (StringUtil.hasText(logfilePath)) {
			File logfile = new File(logfilePath);
			System.err.println("Redirecting log output to: "+logfile);
			System.setErr(new PrintStream(new FileOutputStream(logfile, false)));
		}
	}
	
}
