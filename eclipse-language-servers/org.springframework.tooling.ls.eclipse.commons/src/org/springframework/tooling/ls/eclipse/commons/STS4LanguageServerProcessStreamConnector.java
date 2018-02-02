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
package org.springframework.tooling.ls.eclipse.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.springframework.tooling.ls.eclipse.commons.console.ConsoleUtil.Console;
import org.springframework.tooling.ls.eclipse.commons.console.LanguageServerConsoles;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;

public class STS4LanguageServerProcessStreamConnector extends ProcessStreamConnectionProvider {

	private static LanguageServerProcessReaper processReaper = new LanguageServerProcessReaper();
	
	private Supplier<Console> consoles = null;
	
	public STS4LanguageServerProcessStreamConnector(String consoleLabel) {
		if (consoleLabel!=null) {
			this.consoles = LanguageServerConsoles.getConsoleFactory(consoleLabel);
		}
	}
	
	@Override
	public void start() throws IOException {
		super.start();
		Process process = LanguageServerProcessReaper.getProcess(this);
		processReaper.addProcess(process);
		if (consoles!=null) {
			forwardTo(getLanguageServerLog(), consoles.get().out);
		}
	}
	
	@Override
	protected ProcessBuilder createProcessBuilder() {
		if (consoles==null) {
			return super.createProcessBuilder();
		}
		ProcessBuilder builder = new ProcessBuilder(getCommands());
		builder.directory(new File(getWorkingDirectory()));
		//builder.redirectError(ProcessBuilder.Redirect.INHERIT);
		return builder;
	}
	
	private void forwardTo(InputStream is, OutputStream os) {
		Job consoleJob = new Job("Forward Language Server log output to console") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				try {
					pipe(is, os);
					os.write("==== Process Terminated====\n".getBytes(Charsets.UTF_8));
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally {
					try {
						os.close();
					} catch (IOException e) {
					}
				}
				return Status.OK_STATUS;
			}
			
			void pipe(InputStream input, OutputStream output) throws IOException {
				try {
				    byte[] buf = new byte[1024*4];
				    int n = input.read(buf);
				    while (n >= 0) {
				      output.write(buf, 0, n);
				      n = input.read(buf);
				    }
				    output.flush();
				} finally {
					input.close();
				}
			}

		};
		consoleJob.setSystem(true);
		consoleJob.schedule();
	}

	private InputStream getLanguageServerLog() {
		return super.getErrorStream();
	}
 	
	@Override
	public void stop() {
		super.stop();
		processReaper.removeProcess(LanguageServerProcessReaper.getProcess(this));
	}
	
	protected String getWorkingDirLocation() {
		return System.getProperty("user.dir");
	}
	
}
