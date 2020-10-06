package org.springframework.ide.vscode.xml;

import java.net.URL;

import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4j.InitializeParams;
import org.springframework.ide.vscode.xml.namespaces.ProjectAwareUrlStreamHandlerFactory;
import org.springframework.ide.vscode.xml.namespaces.ProjectClasspathUriResolverExtension;
import org.springframework.ide.vscode.xml.namespaces.classpath.ProjectResourceLoaderCache;

public class SpringXmlPlugin implements IXMLExtension {
	
	private JavaProjectCache javaProjectCache;
	
	@Override
	public void start(InitializeParams params, XMLExtensionsRegistry registry) {
		javaProjectCache = new JavaProjectCache(registry.getCommandService(), registry.getDocumentProvider(), registry.getValidationService());		
		javaProjectCache.start();
		
		ProjectResourceLoaderCache loaderCache = new ProjectResourceLoaderCache(javaProjectCache);
		
		URL.setURLStreamHandlerFactory(new ProjectAwareUrlStreamHandlerFactory(javaProjectCache, loaderCache));
		
		registry.getResolverExtensionManager().registerResolver(new ProjectClasspathUriResolverExtension(javaProjectCache, loaderCache));
	}

	@Override
	public void stop(XMLExtensionsRegistry registry) {
		javaProjectCache.stop();
	}
	
}
