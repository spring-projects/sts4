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
package org.springframework.tooling.jdt.ls.extension;

import static org.springframework.tooling.jdt.ls.extension.Logger.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.runtime.Log;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaClientConnection;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.springframework.tooling.jdt.ls.extension.ClasspathListenerManager.ClasspathListener;

@SuppressWarnings("restriction")
public class ClasspathListenerHandler implements IDelegateCommandHandler {

	static class Subscribptions {

		private static Map<String, ClasspathListenerManager> subscribers = null;
		
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
			new Job("SendClasspath notification") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					log("Classpath changed " + jp.getElementName());
					String project = jp.getProject().getLocationURI().toString();
					boolean deleted = !jp.exists();
					JavaClientConnection conn = JavaLanguageServerPlugin.getInstance().getClientConnection();
					String projectName = jp.getElementName();

					Classpath classpath = null;
					if (!deleted) {
						try {
							classpath = ClasspathUtil.resolve(jp);
						} catch (Exception e) {
							Logger.log(e);
						}
					}
					try {
						Logger.log("executing callback "+callbackCommandId+" "+projectName+" "+deleted+" "+(classpath==null ? "" : classpath.getEntries().size()));
						conn.executeClientCommand(callbackCommandId, project, projectName, deleted, classpath);
						Logger.log("executing callback "+callbackCommandId+" SUCCESS");
					} catch (Exception e) {
						Logger.log("executing callback "+callbackCommandId+" FAILED");
						Logger.log(e);
					}
					return Status.OK_STATUS;
				}
			}
			.schedule();
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
			Logger.log("subsribers = " + subscribers.keySet());
		}
	}
	
	private static Subscribptions subscribptions = new Subscribptions();

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		log("ClasspathListenerHandler executeCommand " + commandId + ", " + arguments);
		switch (commandId) {
		case "sts.java.addClasspathListener":
			return addClasspathListener((String) arguments.get(0));
		case "sts.java.removeClasspathListener":
			return removeClasspathListener((String) arguments.get(0));
		default:
			throw new IllegalArgumentException("Unknown command id: " + commandId);
		}
	}

	private Object removeClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler addClasspathListener " + callbackCommandId);
		subscribptions.unsubscribe(callbackCommandId);
		log("ClasspathListenerHandler addClasspathListener " + callbackCommandId + " => OK");
		return "ok";
	}

	private Object addClasspathListener(String callbackCommandId) {
		log("ClasspathListenerHandler addClasspathListener " + callbackCommandId);
		subscribptions.subscribe(callbackCommandId);
		log("ClasspathListenerHandler addClasspathListener " + callbackCommandId + " => OK");
		return "ok";
	}

}
