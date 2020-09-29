/*******************************************************************************
 * Copyright (c) 2007, 2012, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces;

import org.eclipse.core.resources.IProject;
import org.springsource.ide.eclipse.commons.core.SpringCorePreferences;

public class XmlNamespaceUtils {
	
	public static boolean convertNamespaceLocationsToHttps(IProject project) {
		if (SpringCorePreferences.getProjectPreferences(project, SpringXmlNamespacesPlugin.PLUGIN_ID).getBoolean(
				SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, false)) {
			return SpringCorePreferences.getProjectPreferences(project, SpringXmlNamespacesPlugin.PLUGIN_ID).getBoolean(
					SpringXmlNamespacesPlugin.USE_HTTPS_FOR_SCHEMA_LOCATIONS, false);
		}
		return SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().getBoolean(
				SpringXmlNamespacesPlugin.USE_HTTPS_FOR_SCHEMA_LOCATIONS);
	}

	/**
	 * Converts a given XSD schema location to https if the preference is enabled.
	 */
	public static String convertToHttps(IProject project, String schemaLocation) {
		if (convertNamespaceLocationsToHttps(project) && schemaLocation.startsWith("http:")) {
			return "https"+schemaLocation.substring(4);
		}
		return schemaLocation;
	}

	/**
	 * Checks if a project should load namespace handlers from the classpath or use the global catalog.
	 */
	@SuppressWarnings("deprecation")
	public static boolean useNamespacesFromClasspath(IProject project) {
		if (project == null) {
			return false;
		}

		if (SpringCorePreferences.getProjectPreferences(project, SpringXmlNamespacesPlugin.PLUGIN_ID).getBoolean(
				SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, false)) {
			return SpringCorePreferences.getProjectPreferences(project, SpringXmlNamespacesPlugin.PLUGIN_ID).getBoolean(
					SpringXmlNamespacesPlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, false);
		}
		return SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().getBoolean(
				SpringXmlNamespacesPlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID);
	}
	
	@SuppressWarnings("deprecation")
	public static boolean disableCachingForNamespaceLoadingFromClasspath(IProject project) {
		if (project == null) {
			return false;
		}
		
		if (SpringCorePreferences.getProjectPreferences(project, SpringXmlNamespacesPlugin.PLUGIN_ID).getBoolean(
				SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, false)) {
			return SpringCorePreferences.getProjectPreferences(project, SpringXmlNamespacesPlugin.PLUGIN_ID).getBoolean(
					SpringXmlNamespacesPlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID, false);
		}
		return SpringXmlNamespacesPlugin.getDefault().getPluginPreferences().getBoolean(
				SpringXmlNamespacesPlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID);
	}



}
