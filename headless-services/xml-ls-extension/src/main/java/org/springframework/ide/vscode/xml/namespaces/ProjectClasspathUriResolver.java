package org.springframework.ide.vscode.xml.namespaces;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.ide.vscode.xml.IJavaProjectProvider.IJavaProjectData;
import org.springframework.ide.vscode.xml.namespaces.classpath.ProjectResourceLoaderCache;
import org.springframework.ide.vscode.xml.namespaces.classpath.PropertiesLoaderUtils;
import org.springframework.ide.vscode.xml.namespaces.classpath.ResourceLoader;
import org.springframework.ide.vscode.xml.namespaces.model.NamespaceDefinition;
import org.springframework.ide.vscode.xml.namespaces.util.TargetNamespaceScanner;

/**
 * resolves URIs on the project classpath using the protocol established by
 * <code>spring.schemas</code> files.
 * 
 * @author Martin Lippert
 * @since 2.7.0
 */
public class ProjectClasspathUriResolver {
	
	private static Logger LOGGER = Logger.getLogger(ProjectClasspathUriResolver.class.getName());
	
	private static final String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spring.schemas";


	private final IJavaProjectData project;
	private boolean disableCaching;

	private Map<String, String> typePublic;
	private Map<String, String> typeUri;
	private Map<String, String> schemaMappings;
	private ProjectResourceLoaderCache loaderCache;

	public ProjectClasspathUriResolver(ProjectResourceLoaderCache loaderCache, IJavaProjectData project, boolean disableCaching) {
		this.loaderCache = loaderCache;
		this.project = project;
		this.disableCaching = disableCaching;
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
		ResourceLoader classLoader = loaderCache.getResourceLoader(project, null);
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
		ResourceLoader classLoader = loaderCache.getResourceLoader(project, null);

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
					ProjectAwareUrlStreamHandlerFactory.createProjectAwareUrl(
							project.getName(), resolvedPath)).toURL();
			return TargetNamespaceScanner.getTargetNamespace(url);
		} catch (URISyntaxException | IOException e) {
			LOGGER.log(Level.WARNING, e, null);
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
							DEFAULT_SCHEMA_MAPPINGS_LOCATION,
							classLoader);
			mergePropertiesIntoMap(mappings, handlerMappings);
//		} catch (IOException ex) {
//			// We can ignore this as we simply don't find the xsd file then.
//		}
		return handlerMappings;
	}
	
	@SuppressWarnings("unchecked")
	private static <K, V> void mergePropertiesIntoMap(/*@Nullable*/ Properties props, Map<K, V> map) {
		if (props != null) {
			for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				Object value = props.get(key);
				if (value == null) {
					// Allow for defaults fallback or potentially overridden accessor...
					value = props.getProperty(key);
				}
				map.put((K) key, (V) value);
			}
		}
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
