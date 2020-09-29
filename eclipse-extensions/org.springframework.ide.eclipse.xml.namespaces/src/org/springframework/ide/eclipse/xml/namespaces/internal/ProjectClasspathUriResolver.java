/*******************************************************************************
 * Copyright (c) 2011, 2019 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.xml.namespaces.ProjectAwareUrlStreamHandlerService;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;
import org.springframework.ide.eclipse.xml.namespaces.XmlNamespaceUtils;
import org.springframework.ide.eclipse.xml.namespaces.classpath.ProjectResourceLoaderCache;
import org.springframework.ide.eclipse.xml.namespaces.classpath.PropertiesLoaderUtils;
import org.springframework.ide.eclipse.xml.namespaces.classpath.ResourceLoader;
import org.springframework.ide.eclipse.xml.namespaces.model.NamespaceDefinition;
import org.springframework.ide.eclipse.xml.namespaces.util.TargetNamespaceScanner;
import org.springsource.ide.eclipse.commons.core.util.CollectionUtils;

/**
 * resolves URIs on the project classpath using the protocol established by
 * <code>spring.schemas</code> files.
 * 
 * @author Martin Lippert
 * @since 2.7.0
 */
public class ProjectClasspathUriResolver {

	private final IProject project;
	private boolean disableCaching;

	private Map<String, String> typePublic;
	private Map<String, String> typeUri;
	private Map<String, String> schemaMappings;

	public ProjectClasspathUriResolver(IProject project) {
		this.project = project;
		this.disableCaching = XmlNamespaceUtils
				.disableCachingForNamespaceLoadingFromClasspath(project);

		if (!disableCaching) {
			init();
		}
	}

	/**
	 * Resolves the given <code>systemId</code> on the classpath configured by
	 * the <code>file</code>'s project.
	 */
	public String resolveOnClasspath(String publicId, String systemId) {
		if (disableCaching) {
			return resolveOnClasspathAndSourceFolders(publicId, systemId);
		}
		return resolveOnClasspathOnly(publicId, systemId);
	}

	private String resolveOnClasspathAndSourceFolders(String publicId,
			String systemId) {
		ResourceLoader classLoader = ProjectResourceLoaderCache.getResourceLoader(project, null);
		Map<String, String> mappings = getSchemaMappings(classLoader);
		
		if (mappings != null && systemId != null) {
			if (mappings.containsKey(systemId)) {
				String xsdPath = mappings.get(systemId);
				return resolveXsdPathOnClasspath(xsdPath, classLoader);
			}
			else if (mappings.containsKey(systemId.replace("https://", "http://"))) {
				String xsdPath = mappings.get(systemId.replace("https://", "http://"));
				return resolveXsdPathOnClasspath(xsdPath, classLoader);
			}
			else if (mappings.containsKey(systemId.replace("http://", "https://"))) {
				String xsdPath = mappings.get(systemId.replace("http://", "https://"));
				return resolveXsdPathOnClasspath(xsdPath, classLoader);
			}
		}

		return null;
	}

	private String resolveOnClasspathOnly(String publicId, String systemId) {
		String resolved = null;

		if (systemId != null) {
			resolved = typeUri.get(systemId);
		}
		
		if (resolved == null && systemId != null && systemId.startsWith("https://")) {
			resolved = typeUri.get(systemId.replace("https://", "http://"));
		}
		
		if (resolved == null && systemId != null && systemId.startsWith("http://")) {
			resolved = typeUri.get(systemId.replace("http://", "https://"));
		}

		if (resolved == null && publicId != null) {
			if (!(systemId != null && systemId.endsWith(".xsd"))) {
				resolved = typePublic.get(publicId);
			}
		}
		return resolved;
	}

	private void init() {
		this.typePublic = new ConcurrentHashMap<String, String>();
		this.typeUri = new ConcurrentHashMap<String, String>();

		Map<String, NamespaceDefinition> namespaceDefinitionRegistry = new HashMap<String, NamespaceDefinition>();
		ResourceLoader classLoader = ProjectResourceLoaderCache.getResourceLoader(project, null);

		schemaMappings = getSchemaMappings(classLoader);
		if (schemaMappings != null) {
			for (String key : schemaMappings.keySet()) {
				String path = schemaMappings.get(key);

				// add the resolved path to the list of uris
				String resolvedPath = resolveXsdPathOnClasspath(path,
						classLoader);
				if (resolvedPath != null) {
					typeUri.put(key, resolvedPath);

					// collect base information to later extract the default uri
					String namespaceUri = getTargetNamespace(resolvedPath);

					if (namespaceDefinitionRegistry.containsKey(namespaceUri)) {
						namespaceDefinitionRegistry.get(namespaceUri)
								.addSchemaLocation(key);
						namespaceDefinitionRegistry.get(namespaceUri).addUri(
								path);
					} else {
						NamespaceDefinition namespaceDefinition = new NamespaceDefinition(
								null);
						namespaceDefinition.addSchemaLocation(key);
						namespaceDefinition.setNamespaceUri(namespaceUri);
						namespaceDefinition.addUri(path);
						namespaceDefinitionRegistry.put(namespaceUri,
								namespaceDefinition);
					}
				}
			}

			// Add catalog entry to namespace uri
			for (NamespaceDefinition definition : namespaceDefinitionRegistry
					.values()) {
				String namespaceKey = definition.getNamespaceUri();
				String defaultUri = definition.getDefaultUri();

				String resolvedPath = resolveXsdPathOnClasspath(defaultUri,
						classLoader);
				if (resolvedPath != null) {
					typePublic.put(namespaceKey, resolvedPath);
				}
			}

		}
	}

	/**
	 * Returns the target namespace URI of the XSD identified by the given
	 * <code>resolvedPath</code>.
	 */
	private String getTargetNamespace(String resolvedPath) {
		if (resolvedPath == null) {
			return null;
		}

		try {
			URL url = new URI(
					ProjectAwareUrlStreamHandlerService.createProjectAwareUrl(
							project.getName(), resolvedPath)).toURL();
			return TargetNamespaceScanner.getTargetNamespace(url);
		} catch (URISyntaxException | IOException e) {
			SpringXmlNamespacesPlugin.log(e);
		}
		return null;
	}

	/**
	 * Loads all schema mappings from all <code>spring.schemas</code> files on
	 * the project classpath.
	 * 
	 * @param classLoader
	 *            The classloader that is used to load the properties
	 */
	private Map<String, String> getSchemaMappings(ResourceLoader classLoader) {
		Map<String, String> handlerMappings = new ConcurrentHashMap<String, String>();
//		try {
			Properties mappings = PropertiesLoaderUtils
					.loadAllProperties(
							ProjectClasspathNamespaceDefinitionResolver.DEFAULT_SCHEMA_MAPPINGS_LOCATION,
							classLoader);
			CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);
//		} catch (IOException ex) {
//			// We can ignore this as we simply don't find the xsd file then.
//		}
		return handlerMappings;
	}

	private String resolveXsdPathOnClasspath(String xsdPath, ResourceLoader classLoader) {
		URL url = classLoader.getResource(xsdPath);

		// fallback, if schema location starts with / and therefore fails to be
		// found by classloader
		if (url == null && xsdPath.startsWith("/")) {
			xsdPath = xsdPath.substring(1);
			url = classLoader.getResource(xsdPath);
		}

		return url == null ? null : xsdPath;
	}

}
