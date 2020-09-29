/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * Some {@link IMarker} helper methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class MarkerUtils {

	public static final String ORIGINATING_RESOURCE_KEY = "originatingResource";

	public static final String ELEMENT_ID_KEY = "elementId";

	public static int getHighestSeverityFromMarkersInRange(IResource resource, String markerId,
			int startLine, int endLine) {
		int severity = -1;
		if (resource != null) {
			for (IMarker marker : getAllMarkersInRange(resource, markerId, startLine, endLine)) {
				int sev = marker.getAttribute(IMarker.SEVERITY, -1);
				if (sev == IMarker.SEVERITY_WARNING) {
					severity = sev;
				}
				else if (sev == IMarker.SEVERITY_ERROR) {
					severity = sev;
					break;
				}
			}
		}
		return severity;
	}

	public static Set<IMarker> getAllMarkersInRange(IResource resource, String markerId, int startLine, int endLine) {
		Set<IMarker> foundMarkers = new HashSet<IMarker>();
		if (resource != null) {
			try {
				IMarker[] markers = resource.findMarkers(markerId, true,
						IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					if (startLine == -1 || isMarkerInRange(marker, startLine, endLine)) {
						foundMarkers.add(marker);
					}
				}
			}
			catch (CoreException e) {
				// ignore
			}
		}
		return foundMarkers;
	}

	public static boolean isMarkerInRange(IMarker marker, int startLine, int endLine)
			throws CoreException {
		if (startLine >= 0 && endLine >= startLine && marker.isSubtypeOf(IMarker.TEXT)) {
			int line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
			return (line >= startLine && line <= endLine);
		}
		return false;
	}

	/**
	 * Removes all {@link IMarker markers} with the given id (including the
	 * inherited ones) from given {@link IResource} and it's members that if the
	 * marker has an attribute under the key named
	 * {@link #ORIGINATING_RESOURCE_KEY} attribute that matches the given
	 * {@link IResource#getFullPath()}.
	 */
	public static void deleteAllMarkers(IResource resource, String id) {
		if (resource != null && resource.isAccessible()) {
			try {
				IMarker[] markers = resource.findMarkers(id, true,
						IResource.DEPTH_ONE);
				for (IMarker marker : markers) {
//					if (originatingResourceValue.equals(marker
//							.getAttribute(ORIGINATING_RESOURCE_KEY))) {
						marker.delete();
//					}
				}
			}
			catch (CoreException e) {
				CorePlugin.log(e);
			}
		}
	}

	/**
	 * Removes all {@link IMarker markers} with given id (including the
	 * inherited ones) from given {@link IResource} and it's members.
	 */
	public static void deleteMarkers(IResource resource, String id) {
		if (resource != null && resource.isAccessible()) {
			try {
				resource.deleteMarkers(id, true, IResource.DEPTH_INFINITE);
			}
			catch (CoreException e) {
				CorePlugin.log(e);
			}
		}
	}

}
