/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;

/**
 * Default implementation of {@link INamespaceDefinition}.
 * @author Christian Dupuis
 */
public class NamespaceDefinition implements INamespaceDefinition {

	private Pattern versionPattern = Pattern.compile(".*-([0-9,.]*)\\.xsd");

	private Bundle bundle;

	private String iconPath;

	private String name;

	private String namespaceUri;

	private String prefix;

	private Set<String> schemaLocations = new CopyOnWriteArraySet<String>();

	private Set<String> uris = new CopyOnWriteArraySet<String>();

	private Properties uriMapping = new Properties();

	private String defaultSchemaLocation = null;

	public NamespaceDefinition(Properties uriMapping) {
		this.uriMapping = uriMapping;
	}

	public void addSchemaLocation(String schemaLocation) {
		schemaLocations.add(schemaLocation);
	}

	public void addUri(String uri) {
		uris.add(uri);
	}

	/**
	 * {@inheritDoc}
	 */
	public Bundle getBundle() {
		return bundle;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized String getDefaultSchemaLocation() {
		if (defaultSchemaLocation == null) {
			// Per convention the version-less XSD is the default
			for (String schemaLocation : schemaLocations) {
				if (!versionPattern.matcher(schemaLocation).matches()) {
					defaultSchemaLocation = schemaLocation;
				}
			}
			if (defaultSchemaLocation == null && schemaLocations.size() > 0) {
				List<String> locations = new ArrayList<String>(schemaLocations);
				Collections.sort(locations);
				defaultSchemaLocation = locations.get(0);
			}
		}
		return defaultSchemaLocation;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultUri() {
		String defaultUri = null;
		NamespaceVersion version = NamespaceVersion.MINIMUM_VERSION;
		for (String uri : uris) {
			NamespaceVersion tempVersion = NamespaceVersion.MINIMUM_VERSION;
			Matcher matcher = versionPattern.matcher(uri);
			if (matcher.matches()) {
				tempVersion = new NamespaceVersion(matcher.group(1));
			}
			if (tempVersion.compareTo(version) >= 0) {
				version = tempVersion;
				defaultUri = uri;
			}
		}
		return defaultUri;
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream getIconStream() {
        if (bundle == null || iconPath == null) {
        	return null;
        }

        int bundleState = bundle.getState();
        if (!((bundleState & (Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0)) {
			return null;
		}

        // look for the image (this will check both the plugin and fragment folders
        URL fullPathString = FileLocator.find(bundle, new Path(iconPath), null);
        if (fullPathString == null) {
            try {
                fullPathString = new URL(iconPath);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        
        if (fullPathString != null) {
        	try {
				return fullPathString.openStream();
			}
			catch (IOException e) {
				SpringXmlNamespacesPlugin.log(e);
			}
        }
        return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespaceUri() {
		return namespaceUri;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPrefix() {
		if (prefix != null) {
			return prefix;
		}
		int ix = namespaceUri.lastIndexOf('/');
		if (ix > 0) {
			return namespaceUri.substring(ix + 1);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getSchemaLocations() {
		return schemaLocations;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNamespaceUri(String namespaceUri) {
		this.namespaceUri = namespaceUri;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * {@inheritDoc}
	 */
	public Properties getUriMapping() {
		return this.uriMapping;
	}

	public String getIconPath() {
		return iconPath;
	}
	
	@Override
	public String toString() {
		return this.namespaceUri;
	}
}