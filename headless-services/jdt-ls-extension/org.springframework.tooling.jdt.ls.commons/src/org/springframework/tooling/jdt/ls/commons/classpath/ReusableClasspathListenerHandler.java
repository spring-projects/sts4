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

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.classpath.ClasspathListenerManager.ClasspathListener;

/**
 * {@link ReusableClasspathListenerHandler} is an 'abstracted' version of the jdtls ClasspathListenerHandler. 
 */
public class ReusableClasspathListenerHandler {

	private final ClientCommandExecutor conn;
	private final Logger logger;
	private final Supplier<Comparator<IProject>> projectSorterFactory;
	
	public ReusableClasspathListenerHandler(Logger logger, ClientCommandExecutor conn) {
		this(logger, conn, null);
	}
	
	public ReusableClasspathListenerHandler(Logger logger, ClientCommandExecutor conn, Supplier<Comparator<IProject>> projectSorterFactory) {
		this.conn = conn;
		this.logger = logger;
		this.projectSorterFactory = projectSorterFactory;
		logger.log("Instantiating ReusableClasspathListenerHandler");
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


	class Subscriptions {

		private Set<String> subscribers = null;
		private ClasspathListenerManager classpathListener = null;
		
		public synchronized void subscribe(String callbackCommandId) {
			logger.log("subscribing to classpath changes: " + callbackCommandId);
			if (subscribers==null) {
				//First subscriber
				subscribers = new HashSet<>();
				classpathListener = new ClasspathListenerManager(logger, new ClasspathListener() {
					@Override
					public void classpathChanged(IJavaProject jp) {
						sendNotification(jp, subscribers);
					}
				});
			}
			subscribers.add(callbackCommandId);
			logger.log("subsribers = " + subscribers);
			sendInitialEvents(callbackCommandId);
		}
		
		private void sendInitialEvents(String callbackCommandId) {
			logger.log("Sending initial event for all projects ...");
			
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			if (projectSorterFactory != null) {
				Arrays.sort(projects, projectSorterFactory.get());
			}

			for (IProject p : projects) {
				logger.log("project "+p.getName() +" ..." );
				try {
					if (p.isAccessible() && p.hasNature(JavaCore.NATURE_ID)) {
						IJavaProject jp = JavaCore.create(p);
						sendNotification(jp, Collections.singleton(callbackCommandId));
					} else {
						logger.log("project "+p.getName() +" SKIPPED" );
					}
				} catch (CoreException e) {
					logger.log(e);
				}
			}
			logger.log("Sending initial event for all projects DONE");
		}
	
		private void sendNotification(IJavaProject jp, Collection<String> callbackIds) {
			//TODO: make one Job to accumulate all requested notification and work more efficiently by batching
			// and avoiding multiple executions of duplicated requests.
			Job job = new Job("SendClasspath notification") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					synchronized (projectLocations) { //Could use some Eclipse job rule. But its really a bit of a PITA to create the right one.
						try {
							logger.log("Preparing classpath changed notification " + jp.getElementName());
							URI projectLoc = getProjectLocation(jp);
							if (projectLoc==null) {
								logger.log("Could not send event for project because no project location: "+jp.getElementName());
							} else {
								boolean exsits = projectExists(jp);
								boolean open = true; // WARNING: calling jp.isOpen is unreliable and subject to race condition. After a POST_CHAGE project open event
													// this should be true but it typically is not unless you wait for some time. No idea how you would know
													// how long you should wait (200ms is not enough, and that seems pretty long). Isn't it kind of the point 
													// for a 'POST_CHANGE' event to come **after** model has already changed? I guess not in Eclipse.
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
								for (String callbackCommandId : callbackIds) {
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
			};
			job.schedule();
		}

		public synchronized void unsubscribe(String callbackCommandId) {
			logger.log("unsubscribing from classpath changes: " + callbackCommandId);
			if (subscribers != null) {
				subscribers.remove(callbackCommandId);
				if (subscribers.isEmpty()) {
					subscribers = null;
					if (classpathListener!=null) {
						classpathListener.dispose();
						classpathListener = null;
					}
				}
			}
			logger.log("subsribers = " + subscribers);
		}

		public boolean isEmpty() {
			return subscribers == null || subscribers.isEmpty();
		}
	}
	
	private Subscriptions subscribptions = new Subscriptions();

	public Object removeClasspathListener(String callbackCommandId) {
		logger.log("ClasspathListenerHandler removeClasspathListener " + callbackCommandId);
		subscribptions.unsubscribe(callbackCommandId);
		logger.log("ClasspathListenerHandler removeClasspathListener " + callbackCommandId + " => OK");
		return "ok";
	}

	public Object addClasspathListener(String callbackCommandId) {
		logger.log("ClasspathListenerHandler addClasspathListener " + callbackCommandId);
		subscribptions.subscribe(callbackCommandId);
		logger.log("ClasspathListenerHandler addClasspathListener " + callbackCommandId + " => OK");
		return "ok";
	}

	public boolean hasNoActiveSubscriptions() {
		return subscribptions.isEmpty();
	}

}
