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

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.tooling.jdt.ls.commons.Logger;

/**
 * An instance of this class provides a means to register
 * listeners that get notified when classpath for a IJavaProject
 * changes.
 *
 * @author Kris De Volder
 */
public class ClasspathListenerManager {

	public interface ClasspathListener {
		public abstract void classpathChanged(IJavaProject jp);
	}

	private class MyListener implements IElementChangedListener {

		@Override
		public void elementChanged(ElementChangedEvent event) {
			visit(event.getDelta());
		}

		private void visit(IJavaElementDelta delta) {
			IJavaElement el = delta.getElement();
			switch (el.getElementType()) {
			case IJavaElement.JAVA_MODEL:
				visitChildren(delta);
				break;
			case IJavaElement.JAVA_PROJECT:
				if (isCreatedOrDeleted(delta) || isClasspathChanged(delta.getFlags())) {
					listener.classpathChanged((IJavaProject)el);
				}
				break;
			default:
				break;
			}
		}

		private boolean isCreatedOrDeleted(IJavaElementDelta delta) {
			int kind = delta.getKind();
			return kind == IJavaElementDelta.ADDED || kind==IJavaElementDelta.REMOVED;
		}

		private boolean isClasspathChanged(int flags) {
			return 0!= (flags & (
					IJavaElementDelta.F_CLASSPATH_CHANGED |
					IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED |
					IJavaElementDelta.F_CLOSED |
					IJavaElementDelta.F_OPENED
			));
		}

		public void visitChildren(IJavaElementDelta delta) {
			for (IJavaElementDelta c : delta.getAffectedChildren()) {
				visit(c);
			}
		}
	}

	private ClasspathListener listener;
	private MyListener myListener;
	private final Logger logger;

	public ClasspathListenerManager(Logger logger, ClasspathListener listener) {
		this.logger = logger;
		logger.log("Setting up ClasspathListenerManager");
		this.listener = listener;
		JavaCore.addElementChangedListener(myListener=new MyListener(), ElementChangedEvent.POST_CHANGE);
	}

	public void dispose() {
		if (myListener!=null) {
			JavaCore.removeElementChangedListener(myListener);
			myListener = null;
		}
	}

}