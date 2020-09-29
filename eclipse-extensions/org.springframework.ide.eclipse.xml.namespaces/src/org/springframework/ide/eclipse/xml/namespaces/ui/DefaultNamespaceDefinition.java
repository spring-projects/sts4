/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.ui;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;
import org.springsource.ide.eclipse.commons.core.JdtUtils;
import org.springsource.ide.eclipse.commons.core.SpringCorePreferences;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;

/**
 * Default implementation of {@link INamespaceDefinition}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings({ "deprecation", "restriction" })
public class DefaultNamespaceDefinition implements INamespaceDefinition {

	public static final Pattern VERSION_PATTERN = Pattern.compile(".*-([0-9,.]*)\\.xsd");

	private final String defaultLocation;

	private final IImageAccessor imageAccessor;

	private Set<String> locations = new HashSet<String>();

	private final String defaultPrefix;

	private final String uri;

	private Properties uriMapping = new Properties();

	public DefaultNamespaceDefinition(String prefix, String uri, String defaultLocation, IImageAccessor imageAccessor) {
		this(prefix, uri, defaultLocation, new Properties(), imageAccessor);
	}

	public DefaultNamespaceDefinition(String prefix, String uri, String defaultLocation, final Image image) {
		this(prefix, uri, defaultLocation, new IImageAccessor() {

			public Image getImage() {
				return image;
			}
		});
	}

	public DefaultNamespaceDefinition(String prefix, String uri, String defaultLocation,
			Properties namespaceDefinition, IImageAccessor imageAccessor) {
		if (prefix != null) {
			this.defaultPrefix = prefix;
		}
		else {
			int ix = uri.lastIndexOf('/');
			this.defaultPrefix = uri.substring(ix + 1);
		}
		this.uri = uri;
		this.defaultLocation = defaultLocation;
		this.uriMapping = namespaceDefinition;
		this.imageAccessor = imageAccessor;
	}

	public void addSchemaLocation(String location) {
		locations.add(location);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (obj instanceof DefaultNamespaceDefinition) {
			DefaultNamespaceDefinition o = (DefaultNamespaceDefinition) obj;
			return o.defaultPrefix.equals(defaultPrefix) && o.uri.equals(uri);
		}
		return false;
	}

	public String getDefaultNamespacePrefix() {
		return defaultPrefix;
	}

	public String getDefaultSchemaLocation() {
		return getDefaultSchemaLocation(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultSchemaLocation(IResource resource) {
		String location = null;
		if (hasProjectSpecificOptions(resource)) {
			SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(resource.getProject(),
					SpringXmlNamespacesPlugin.PLUGIN_ID);
			if (prefs.getBoolean(SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, true)) {
				location = getDefaultSchemaLocationFromClasspath(resource);
			}
			if ("".equals(location) || location == null) {
				location = prefs.getString(SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + getNamespaceURI(),
						"");
			}
		}
		else {
			Preferences prefs = SpringXmlNamespacesPlugin.getDefault().getPluginPreferences();
			if (prefs.getBoolean(SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID)) {
				location = getDefaultSchemaLocationFromClasspath(resource);
			}
			if ("".equals(location) || location == null) {
				location = prefs.getString(SpringXmlNamespacesPlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + getNamespaceURI());
			}
		}
		if ("".equals(location) || location == null) {
			return defaultLocation;
		}
		else {
			return location;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getNamespaceImage() {
		if (imageAccessor != null) {
			return imageAccessor.getImage();
		}
		return XmlNamespacesUIImages.getImage(XmlNamespacesUIImages.IMG_OBJS_XSD);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespacePrefix() {
		return defaultPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespacePrefix(IResource resource) {
		String prefix = null;
		if (hasProjectSpecificOptions(resource)) {
			SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(resource.getProject(),
					SpringXmlNamespacesPlugin.PLUGIN_ID);
			prefix = prefs.getString(SpringXmlNamespacesPlugin.NAMESPACE_PREFIX_PREFERENCE_ID + getNamespaceURI(), "");
		}
		else {
			Preferences prefs = SpringXmlNamespacesPlugin.getDefault().getPluginPreferences();
			prefix = prefs.getString(SpringXmlNamespacesPlugin.NAMESPACE_PREFIX_PREFERENCE_ID + getNamespaceURI());
		}
		if (StringUtils.hasLength(prefix)) {
			return prefix;
		}
		return defaultPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespaceURI() {
		return uri;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getSchemaLocations() {
		return locations;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return defaultPrefix.hashCode() ^ uri.hashCode();
	}

	private String getDefaultSchemaLocationFromClasspath(IResource resource) {
		IJavaProject jp = JdtUtils.getJavaProject(resource);
		Set<String> existingLocations = new HashSet<String>();
		try {
			for (Map.Entry<Object, Object> entry : uriMapping.entrySet()) {

				// Check that we are looking for relevant entries only; e.g. util, tool and beans are in the same
				// package
				if (((String) entry.getKey()).startsWith(uri)) {
					String fileLocation = (String) entry.getValue();

					// Get the package name from the location
					String packageName = "";
					int ix = fileLocation.lastIndexOf('/');
					if (ix > 0) {
						packageName = fileLocation.substring(0, ix).replace('/', '.');
					}

					// Search in all project packages
					for (IPackageFragmentRoot root : jp.getPackageFragmentRoots()) {
						IPackageFragment pf = root.getPackageFragment(packageName);
						if (pf.exists()) {
							for (Object obj : pf.getNonJavaResources()) {
								// Entry is coming from a JAR file on the classpath
								if (obj instanceof JarEntryFile
										&& ("/" + fileLocation).equals((((JarEntryFile) obj).getFullPath().toString()))
										&& locations.contains(entry.getKey())) {
									existingLocations.add((String) entry.getKey());
								}
								// Entry is coming from the local project
								else if (obj instanceof IFile
										&& fileLocation.equals(pf.getElementName().replace('.', '/') + "/"
												+ ((IFile) obj).getName()) && locations.contains(entry.getKey())) {
									existingLocations.add((String) entry.getKey());
								}
							}
						}
					}
				}
			}
		}
		catch (JavaModelException e) {
			// We have a safe fall-back
		}

		// Search for the highest version among the existing resources
		String highestLocation = null;
		Version version = Version.MINIMUM_VERSION;
		for (String location : existingLocations) {
			Version tempVersion = Version.MINIMUM_VERSION;
			Matcher matcher = VERSION_PATTERN.matcher(location);
			if (matcher.matches()) {
				tempVersion = new Version(matcher.group(1));
			}
			if (tempVersion.compareTo(version) >= 0) {
				version = tempVersion;
				highestLocation = location;
			}
		}
		return highestLocation;
	}

	private boolean hasProjectSpecificOptions(IResource resource) {
		if (resource == null) {
			return false;
		}
		return SpringCorePreferences.getProjectPreferences(resource.getProject(), SpringXmlNamespacesPlugin.PLUGIN_ID)
				.getBoolean(SpringXmlNamespacesPlugin.PROJECT_PROPERTY_ID, false);
	}

	static class Version implements Comparable<Version> {

		private static final String MINIMUM_VERSION_STRING = "0";

		public static final Version MINIMUM_VERSION = new Version(MINIMUM_VERSION_STRING);

		private final org.osgi.framework.Version version;

		public Version(String v) {
			org.osgi.framework.Version tempVersion = null;
			try {
				tempVersion = org.osgi.framework.Version.parseVersion(v);
			}
			catch (Exception e) {
				// make sure that we don't crash on any new version numbers format that we don't support
				SpringXmlNamespacesPlugin.log("Cannot convert schema vesion", e);
				tempVersion = org.osgi.framework.Version.parseVersion(MINIMUM_VERSION_STRING);
			}
			this.version = tempVersion;
		}

		public int compareTo(Version v2) {
			return this.version.compareTo(v2.version);
		}
	}

}
