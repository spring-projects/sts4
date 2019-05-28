/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.classpath;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.tooling.jdt.ls.commons.Logger;

public class SendClasspathNotificationsJob extends Job {
	
	private final ClientCommandExecutor conn;
	private final Logger logger;
	private String callbackCommandId;
	private boolean isBatched;

	public SendClasspathNotificationsJob(Logger logger, ClientCommandExecutor conn, String callbackId, boolean isBatched) {
		super("Send Classpath Notifications");
		this.logger = logger;
		this.conn = conn;
		this.callbackCommandId = callbackId;
		this.isBatched = isBatched;
	}

	/**
	 * To keep track of project locations. Without this we can't properly handle deletion events because
	 * deleted projects no longer have a location. So we can only send a proper 'project with this location'
	 * was deleted' event if we keep track of project locations ourselves.
	 */
	private Map<String, URI> projectLocations = new HashMap<>();
	
	private URI getProjectLocation(IJavaProject jp) {
		URI loc = jp.getProject().getLocationURI();
		if (loc!=null) {
			return loc;
		} else {
			synchronized (projectLocations) {
				//fallback on what we stored ourselves.
				return projectLocations.get(jp.getElementName());
			}
		}
	}

	public final Queue<IJavaProject> queue = new ConcurrentLinkedQueue<>();
	
	private boolean projectExists(IJavaProject jp) {
		//We can't really deal with projects that don't exist in disk. So using this more strict 'exists' check
		//makes sure anything that looks like it doesn't exist on disk is treated as if it simply doesn't exist 
		//at all. This kind of addresses a issue caused by Eclipse's idiotic behavior when it comes to deleting
		//a project's files from the file system... Eclipse recreates a 'vanilla' project in the workspace,
		//simply refusing to accept the fact that the project is actually gone.
		if (jp.exists()) {
			try {
				URI loc = getProjectLocation(jp);
				if (loc!=null) {
					File f = new File(loc);
					return f.isDirectory() && jp.getProject().hasNature(JavaCore.NATURE_ID);
				}
			} catch (Exception e) {
				//Something bogus about this project... so just pretend it doesn't exist.
			}
		}
		return false;
	}

	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		synchronized (projectLocations) { //Could use some Eclipse job rule. But its really a bit of a PITA to create the right one.
			try {
				for (IJavaProject jp = queue.poll(); jp!=null; jp = queue.poll()) {
					logger.log("Preparing classpath changed notification " + jp.getElementName());
					URI projectLoc = getProjectLocation(jp);
					if (projectLoc==null) {
						logger.log("Could not send event for project because no project location: "+jp.getElementName());
					} else {
						boolean exsits = projectExists(jp);
						boolean open = true; // WARNING: calling jp.isOpen is unreliable and subject to race condition. After a POST_CHAGE project open event
											// this should be true but it typically is not unless you wait for some time. No idea how you would know
											// how long you should wait (200ms is not enough, and that seems pretty long).
											// So we will just pretend / assume project is always open. If resolving classpath fails because it is not
											// open... so be it (there will be no classpath... this is expected for closed project, so that is fine).
						boolean deleted = !(exsits && open);
						logger.log("exists = "+exsits +" open = "+open +" => deleted = "+deleted);
						String projectName = jp.getElementName();

						Classpath classpath = Classpath.EMPTY;
						if (deleted) {
							// projectLocations.remove(projectName);
						} else {
							projectLocations.put(projectName, projectLoc);
							try {
								classpath = ClasspathUtil.resolve(jp, logger);
							} catch (Exception e) {
								logger.log(e);
							}
						}
						try {
							logger.log("executing callback "+callbackCommandId+" "+projectName+" "+deleted+" "+ classpath.getEntries().size());
							Object r = conn.executeClientCommand(callbackCommandId, projectLoc.toString(), projectName, deleted, classpath);
							logger.log("executing callback "+callbackCommandId+" SUCCESS ["+r+"]");
						} catch (Exception e) {
							logger.log("executing callback "+callbackCommandId+" FAILED");
							logger.log(e);
						}
					}
				}
			} catch (Exception e) {
				logger.log(e);
			}
			return Status.OK_STATUS;
		}
	}
}
