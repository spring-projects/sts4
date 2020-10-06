package org.springframework.ide.vscode.xml.namespaces.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamespaceDefinition {

	private Pattern versionPattern = Pattern.compile(".*-([0-9,.]*)\\.xsd");

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

	@Override
	public String toString() {
		return this.namespaceUri;
	}
}