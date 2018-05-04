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
package org.springframework.tooling.jdt.ls.commons.classpath;

import static org.springframework.tooling.jdt.ls.commons.Logger.log;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.classpath.ClasspathListenerManager.ClasspathListener;

/**
 * {@link ReusableClasspathListenerHandler} is an 'abstracted' version of the jdtls ClasspathListenerHandler. 
 */
public class ReusableClasspathListenerHandler {

	private ClientCommandExecutor conn;
	
	public ReusableClasspathListenerHandler(ClientCommandExecutor conn) {
		this.conn = conn;
		log("Instantiating ReusableClasspathListenerHandler");
	}
	
	/**
	 * To keep track of project locations. Without this we can't properly handle deleting events because
	 * deleted projects (no longer) have a location. So we can only send a proper 'project with this location'
	 * was deleted' events if we keep track of project locations ourselves.
	 */
	private Map<String, URI> projectLocations = new HashMap<>();
	
	private URI getProjectLocation(IJavaProject jp) {
		URI loc = jp.getProject().getLocationURI();
		if (loc!=null) {
			return loc;
		} else {
			//fallback on what we stored ourselves.
			return projectLocations.get(jp.getElementName());
		}
	}


	class Subscribptions {

		private Map<String, ClasspathListenerManager> subscribers = null;
		
		public synchronized void subscribe(String callbackCommandId) {
			Logger.log("subscribing to classpath changes: " + callbackCommandId);
			if (subscribers==null) {
				subscribers = new HashMap<>(1);
			}
			subscribers.computeIfAbsent(callbackCommandId, (cid) -> new ClasspathListenerManager(new ClasspathListener() {
				@Override
				public void classpathChanged(IJavaProject jp) {
					sendNotification(callbackCommandId, jp);
				}
			}, true));
			Logger.log("subsribers = " + subscribers.keySet());
		}
	
		private void sendNotification(String callbackCommandId, IJavaProject jp) {
			//TODO: make one Job to accumulate all requested notification and work more efficiently by batching
			// and avoiding multiple executions of duplicated requests.
			Job job = new Job("SendClasspath notification") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					synchronized (projectLocations) { //Could use some Eclipse job rule. But its really a bit of a PITA to create the right one.
						try {
							log("Preparing classpath changed notification " + jp.getElementName());
							URI projectLoc = getProjectLocation(jp);
							if (projectLoc==null) {
								Logger.log("Could not send event for project because no project location: "+jp.getElementName());
							} else {
								boolean exsits = jp.exists();
								boolean open = true; // WARNING: calling is jp.isOpen is unreliable and subject to race condition. After a POST_CHAGE project open event
													// this should be true but it typically is not unless you wait for some time. No idea how you would know
													// how long you should wait (200ms is not enough, and that seems pretty long). Isn't it kind of the point 
													// for a 'POST_CHANGE' event to come **after** model has already changed? I guess not in Eclipse.
													// So we will just pretend / assume project is always open. If resolving classpath fails because it is not
													// open... so be it (there will be no classpath... this is expected for closed project, so that is fine).
								boolean deleted = !(exsits && open);
								Logger.log("exists = "+exsits +" open = "+open +" => deleted = "+deleted);
								String projectName = jp.getElementName();
	
								Classpath classpath = null;
								if (deleted) {
									projectLocations.remove(projectName);
								} else {
									projectLocations.put(projectName, projectLoc);
									try {
										classpath = ClasspathUtil.resolve(jp);
									} catch (Exception e) {
										Logger.log(e);
									}
								}
								try {
									Logger.log("executing callback "+callbackCommandId+" "+projectName+" "+deleted+" "+(classpath==null ? "" : classpath.getEntries().size()));
									conn.executeClientCommand(callbackCommandId, projectLoc.toString(), projectName, deleted, classpath);
									Logger.log("executing callback "+callbackCommandId+" SUCCESS");
								} catch (Exception e) {
									Logger.log("executing callback "+callbackCommandId+" FAILED");
									Logger.log(e);
								}
							}
						} catch (Exception e) {
							Logger.log(e);
						}
						return Status.OK_STATUS;
					}
				}
			};
			job.schedule();
		}

		public synchronized void unsubscribe(String callbackCommandId) {
			Logger.log("unsubscribing from classpath changes: " + callbackCommandId);
			if (subscribers != null) {
				ClasspathListenerManager mgr = subscribers.remove(callbackCommandId);
				if (mgr!=null) {
					mgr.dispose();
				}
				if (subscribers.isEmpty()) {
					subscribers = null;
				}
			}
			Logger.log("subsribers = " + (subscribers == null ? "null" : subscribers.keySet()));
		}

		public boolean isEmpty() {
			return subscribers == null || subscribers.isEmpty();
		}
	}
	
	private Subscribptions subscribptions = new Subscribptions();

	public Object removeClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler removeClasspathListener " + callbackCommandId);
		subscribptions.unsubscribe(callbackCommandId);
		log("ClasspathListenerHandler removeClasspathListener " + callbackCommandId + " => OK");
		return "ok";
	}

	public Object addClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler addClasspathListener " + callbackCommandId);
		subscribptions.subscribe(callbackCommandId);
		log("ClasspathListenerHandler addClasspathListener " + callbackCommandId + " => OK");
		return "ok";
	}

	public boolean hasNoActiveSubscriptions() {
		return subscribptions.isEmpty();
	}

}
