/*******************************************************************************
 * Copyright (c) 2012, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * Encapsulates information about an 'external' command that can be run through the OS. 
 * <p>
 * This is a simplistic implementation. A more sophisticate implementation should allow for
 * different OS's (commands may return different information depending on the OS).
 * 
 * @author Kris De Volder
 */
public class ExternalCommand {

	private final String[] command;

	public ExternalCommand(String... command) {
		ArrayList<String> pieces = new ArrayList<String>(command.length);
		for (String piece : command) {
			if (piece!=null) {
				pieces.add(piece);
			}
		}
		this.command = pieces.toArray(new String[pieces.size()]);
	}
	
	public String[] getProgramAndArgs() {
		return command;
	}
	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for (String piece : command) {
			if (!first) {
				buf.append(" ");
			}
			buf.append(piece);
			first = false;
		}
		return buf.toString();
	}

	/**
	 * Just before executing the command with a ProcessBuilder instance, this method is called,
	 * giving the command a chance to apply some extra configuration (e.g. set some environment
	 * parameters). 
	 */
	public void configure(ProcessBuilder processBuilder) {
		//Default implementation does nothing. Subclasses may override.
	}

	/**
	 * A convenient way to execute commands suitable for use in tests. The output and
	 * result of commands are logged to the console and if the command returns non
	 * 0 exit value an exception is thrown.
	 */
	public void exec(File workdir) throws IOException, InterruptedException, TimeoutException {
		new ExternalProcess(workdir, this);
//		org.junit.Assert.assertEquals(0, process.getExitValue());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(command);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExternalCommand other = (ExternalCommand) obj;
		if (!Arrays.equals(command, other.command))
			return false;
		return true;
	}

	
}
