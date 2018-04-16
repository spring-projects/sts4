/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Poor man's logger which writes log output for jdt.ls extension into a predictable location.
 */
public class Logger {

	private static PrintWriter printwriter;

	static {
		File file = new File(System.getProperty("java.io.tmpdir"));
		file = new File(file, "stsjdt.log");
		try {
			printwriter = new PrintWriter(new FileOutputStream(file), true);
			log("======== "+new Date()+" =======");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void log(String message) {
		printwriter.println(message);
		printwriter.flush();
	}

	public static void log(Exception e) {
		e.printStackTrace(printwriter);
	}

}
