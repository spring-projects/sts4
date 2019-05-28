/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import java.util.function.Supplier;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;


/**
 * Poor man's logger with a default implementation writes log output for jdt.ls extension into a predictable location.
 */
public interface Logger {
	
	public static Logger DEFAULT = new DefaultLogger(false);

	public static class DefaultLogger implements Logger {
		private PrintWriter printwriter;
		public DefaultLogger(boolean USE_SYS_ERR) {
			if (USE_SYS_ERR) {
				printwriter = new PrintWriter(System.err);
			} else {
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
		}
		@Override
		public void log(String message) {
			printwriter.println(message);
			printwriter.flush();
		}

		@Override
		public void log(Exception e) {
			e.printStackTrace(printwriter);
		}
	}

	static Logger forEclipsePlugin(Supplier<Plugin> instance) {
		return new Logger() {

			@Override
			public void log(String message) {
				try {
					Plugin plugin = instance.get();
					plugin.getLog().log(new Status(IStatus.INFO, plugin.getBundle().getSymbolicName(), message));
				} catch (Exception ignore) {
					//Eclipse state is fubar... send log message someplace else.
					DEFAULT.log(message);
				}
			}

			@Override
			public void log(Exception e) {
				try {
					Plugin plugin = instance.get();
					plugin.getLog().log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), "", e));
				} catch (Exception ignore) {
					//Eclipse state is fubar... send log message someplace else.
					DEFAULT.log(e);
				}
			}
			
		};
	}
	

	public static class TestLogger extends DefaultLogger {

		private Exception firstError;

		public TestLogger() {
			super(true);
		}
		
		@Override
		public void log(Exception e) {
			super.log(e);
			if (firstError!=null) {
				firstError = e;
			}
		}
		
		public void assertNoErrors() throws Exception {
			if (firstError!=null) {
				throw firstError;
			}
		}
	}

	void log(String message);
	void log(Exception e);
	
}
