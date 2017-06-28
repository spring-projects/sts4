/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.cloudfoundry.manifest.ls;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.Platform;

/**
 * A 'last resort cleanup' utility for LSP server processes. This class, when instantiated
 * registers a JVM shutdown hook. When the hook is executed it kills of all tracked processes
 * with the most aggressive process termination method available through the JRE.
 * <p>
 * This is meant only as a kind of 'fail-safe' as it is expected that lps4e does proper
 * house-keeping and cleans up these processes automatically and cleanly. So... unless
 * something goes wrong inside lsp4e then the `destroyProcesses` method really should
 * just be iterating an empty collection of processes. I.e. the typical debug output
 * on shutdown should look something like this:
 * 
 * <pre>
 *  LanguageServerProcessReaper: Destroying errant processes... 
 *  LanguageServerProcessReaper: Number of alive processes = 0
 *  LanguageServerProcessReaper: Destroying errant processes... DONE
 * </pre> 
 * 
 * @author Kris De Volder
 */
public class LanguageServerProcessReaper extends Thread {
	
	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println("LanguageServerProcessReaper: "+string);
		}
	}

	LinkedList<Process> processes = new LinkedList<>();
	{
		Runtime.getRuntime().addShutdownHook(this);
	}
	
	public synchronized void addProcess(Process process) {
		debug("added process: "+process);
		if (process!=null) {
			processes.add(process);
		}
		garbageCollect();
	}

	/**
	 * If processes are managed reasonably by lsp4e then we don't expect the list of
	 * actually 'alive' processes to keep on growing. This checks for and removes
	 * processes that are already dead.
	 */
	protected void garbageCollect() {
		Process process;
		Iterator<Process> iter = processes.iterator();
		while (iter.hasNext()) {
			process = iter.next();
			if (!process.isAlive()) {
				iter.remove();
			}
		}
		debug("Number of alive processes = "+processes.size());
	}

	public synchronized void removeProcess(Process process) {
		debug("removeProcess "+process);
		garbageCollect();
	}

	@Override
	public void run() {
		destroyProcesses();
	}

	private synchronized void destroyProcesses() {
		debug("Destroying errant processes... ");
		garbageCollect();
		for (Process process : processes) {
			try {
				debug("Destroying process "+process);
				process.destroyForcibly();
			} catch (Throwable e) {
			}
		}
		debug("Destroying errant processes... DONE");
	}

}
