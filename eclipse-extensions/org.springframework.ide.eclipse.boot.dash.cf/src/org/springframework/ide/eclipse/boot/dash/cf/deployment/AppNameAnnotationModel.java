/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import java.util.Iterator;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;

/**
 * Application Name annotations model
 *
 * @author Alex Boyko
 *
 */
public class AppNameAnnotationModel extends AnnotationModel {

	/**
	 * Annotation model key for attachment to viewer visual annotations model
	 */
	public static final Object APP_NAME_MODEL_KEY = new Object();

	/**
	 * Constant application name. If not <code>null</code> then corresponding annotation must be selected
	 */
	public final String fixedAppName;

	/**
	 * Creates a new, empty projection annotation model.
	 */
	public AppNameAnnotationModel(String fixedAppName) {
		this.fixedAppName = fixedAppName;
	}

	/**
	 * Marks the given annotation as selected. An appropriate
	 * annotation model change event is sent out.
	 *
	 * @param annotation the annotation
	 */
	public void markSelected(Annotation annotation) {
		if (annotation instanceof AppNameAnnotation) {
			AppNameAnnotation appName = (AppNameAnnotation) annotation;
			Iterator<?> iterator= getAnnotationIterator();
			while(iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof AppNameAnnotation && appName != o) {
					AppNameAnnotation a = (AppNameAnnotation) o;
					if (a.isSelected()) {
						a.markUnselected();
						modifyAnnotation(a, true);
					}
				}
			}
			if (!appName.isSelected()) {
				appName.markSelected();
				modifyAnnotation(appName, true);
			}
		}
	}

	/**
	 * Finds the first selected annotation
	 *
	 * @return the selected annotation
	 */
	public AppNameAnnotation getSelectedAppAnnotation() {
		Iterator<?> iterator= getAnnotationIterator();
		while(iterator.hasNext()) {
			Object o = iterator.next();
			if (o instanceof AppNameAnnotation) {
				AppNameAnnotation a = (AppNameAnnotation) o;
				if (a.isSelected()) {
					return a;
				}
			}
		}
		return null;
	}

	/**
	 * Finds application name annotation corresponding to the passed in
	 * application name text parameter
	 *
	 * @param text Application name text
	 * @return annotation corresponding to the application name text
	 */
	public AppNameAnnotation getAnnotation(String text) {
		if (text != null) {
			Iterator<?> iterator= getAnnotationIterator();
			while(iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof AppNameAnnotation) {
					AppNameAnnotation a = (AppNameAnnotation) o;
					if (text.equals(a.getText())) {
						return a;
					}
				}
			}
		}
		return null;
	}

}
