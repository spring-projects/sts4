/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.5.2
 */
public final class StandardProcessRunner implements ProcessRunner {

	private static final String[] ENVIRONMENT = new String[] {
			String.format("JAVA_HOME=%s", System.getProperty("java.home")),
			String.format("PATH=%s", System.getenv("PATH")),
			String.format("USERDOMAIN=%s", System.getenv("USERDOMAIN")),
			String.format("USERNAME=%s", System.getenv("USERNAME")),
			String.format("USERPROFILE=%s", System.getenv("USERPROFILE")) };

	private final OutputWriter[] standardOutputWriters;

	private final OutputWriter[] standardErrorWriters;

	public StandardProcessRunner() {
		this(new OutputWriter[0], new OutputWriter[0]);
	}

	public StandardProcessRunner(OutputWriter standardOutputWriter, OutputWriter standardErrorWriter) {
		this(new OutputWriter[] { standardOutputWriter }, new OutputWriter[] { standardErrorWriter });
	}

	public StandardProcessRunner(OutputWriter[] standardOutputWriters, OutputWriter[] standardErrorWriters) {
		this.standardOutputWriters = standardOutputWriters;
		this.standardErrorWriters = standardErrorWriters;
	}

	public int run(File workingDir, String... command) throws IOException, InterruptedException {
		Process process;

		if (command.length == 1) {
			process = Runtime.getRuntime().exec(command[0], ENVIRONMENT, workingDir);
		}
		else {
			process = Runtime.getRuntime().exec(command, ENVIRONMENT, workingDir);
		}

		CountDownLatch latch = new CountDownLatch(2);
		new Thread(new StreamReader(process.getInputStream(), this.standardOutputWriters, latch)).start();
		new Thread(new StreamReader(process.getErrorStream(), this.standardErrorWriters, latch)).start();

		int returnCode = process.waitFor();
		latch.await();

		return returnCode;
	}

	private static final class StreamReader implements Runnable {

		private final BufferedReader input;

		private final OutputWriter[] outputs;

		private final CountDownLatch latch;

		public StreamReader(InputStream input, OutputWriter[] outputs, CountDownLatch latch) {
			this.input = new BufferedReader(new InputStreamReader(input));
			this.outputs = outputs;
			this.latch = latch;
		}

		public void run() {
			try {
				String line = null;
				while ((line = this.input.readLine()) != null) {
					for (OutputWriter output : this.outputs) {
						output.write(line);
					}
				}
			}
			catch (IOException e) {
			}
			finally {
				try {
					if (this.input != null) {
						this.input.close();
					}
				}
				catch (IOException e) {
				}
				finally {
					this.latch.countDown();
				}
			}
		}
	}
}
