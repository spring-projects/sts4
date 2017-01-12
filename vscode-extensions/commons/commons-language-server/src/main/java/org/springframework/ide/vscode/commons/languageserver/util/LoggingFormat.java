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

package org.springframework.ide.vscode.commons.languageserver.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.StringJoiner;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggingFormat extends Formatter {
	public static ThreadLocal<Integer> request = new ThreadLocal<>();

	@Override
	public String format(LogRecord record) {
		StringJoiner joiner = new StringJoiner("\t");

		joiner.add(record.getLevel().getName());
		joiner.add(requestAsString());
		joiner.add(Thread.currentThread().getName());
		joiner.add(Instant.ofEpochMilli(record.getMillis()).toString());
		//        joiner.add(record.getLoggerName());
		joiner.add(record.getSourceClassName() + "#" + record.getSourceMethodName());
		joiner.add(record.getMessage());

		String result = joiner.toString() + "\n";

		Throwable thrown = record.getThrown();

		if (thrown != null) {
			StringWriter stackTrace = new StringWriter();
			PrintWriter print = new PrintWriter(stackTrace);

			thrown.printStackTrace(print);
			print.flush();

			result = result + stackTrace + "\n";
		}

		return result;
	}

	private CharSequence requestAsString() {
		Integer i = request.get();

		if (i == null)
			return "?";
		else
			return Integer.toString(i);
	}

	private static boolean started = false;

	public static void startLogging() throws IOException {
		if (!started) {
			started = true;

			Logger root = Logger.getLogger("");
//			for (Handler h : ImmutableList.copyOf(root.getHandlers())) {
//				root.removeHandler(h);
//			}
//			FileHandler file = new FileHandler("sts4-extension.%g.log", 100_000, 1, false);
//			root.addHandler(file);

			for (Handler h : root.getHandlers())
				h.setFormatter(new LoggingFormat());
		}
	}
}
