package org.springframework.ide.vscode.xml.namespaces;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import org.springframework.ide.vscode.xml.IJavaProjectProvider;
import org.springframework.ide.vscode.xml.IJavaProjectProvider.IJavaProjectData;
import org.springframework.ide.vscode.xml.namespaces.classpath.ProjectResourceLoaderCache;
import org.springframework.ide.vscode.xml.namespaces.classpath.ResourceLoader;

public class ProjectAwareUrlStreamHandlerFactory implements URLStreamHandlerFactory {
	
	public static final String PROJECT_AWARE_PROTOCOL = "project-aware";
	public static final String PROJECT_AWARE_PROTOCOL_HEADER = PROJECT_AWARE_PROTOCOL + "://";
	
	private IJavaProjectProvider javaProjectProvider;
	private ProjectResourceLoaderCache loaderCache;
	
	public ProjectAwareUrlStreamHandlerFactory(IJavaProjectProvider javaProjectProvider, ProjectResourceLoaderCache loaderCache) {
		this.javaProjectProvider = javaProjectProvider;
		this.loaderCache = loaderCache;
	}

	@Override
	public URLStreamHandler createURLStreamHandler(String protocol) {
		if (PROJECT_AWARE_PROTOCOL.equals(protocol)) {
			return new URLStreamHandler() {
				
				@Override
				protected URLConnection openConnection(URL u) throws IOException {
					String systemId = u.toString();
					String nameAndLocation = systemId.substring(PROJECT_AWARE_PROTOCOL_HEADER.length());
					String projectName = nameAndLocation.substring(0, nameAndLocation.indexOf('/'));
					IJavaProjectData project = javaProjectProvider.get(projectName);
					String resourceId = nameAndLocation.substring(nameAndLocation.indexOf('/') + 1);
					ResourceLoader cl = loaderCache.getResourceLoader(project, null);
					URL resource = cl.getResource(resourceId);
					if (resource != null) {
						return resource.openConnection();
					}
					return null;
				}
			};
		}
		return null;
	}
	
	/**
	 * Creates a string representation of a project-aware protocol URL from
	 * project name and a resource name
	 * 
	 * @param projectName
	 *            spring project name
	 * @param resourceName
	 *            class loader resource name
	 * @return URL string
	 */
	public static String createProjectAwareUrl(String projectName, String resourceName) {
		StringBuilder sb = new StringBuilder();
		sb.append(PROJECT_AWARE_PROTOCOL_HEADER);
		sb.append(projectName);
		sb.append('/');
		sb.append(resourceName);
		return sb.toString();
	}

}
