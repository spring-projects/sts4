package org.springframework.ide.vscode.xml.namespaces.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.stream.Stream;

/**
 * Subset of {@link ClassLoader} interface. Mimicks behavior of
 * classloaders but only can be used to load 'resources' as data.
 * 
 * @author Kris De Volder
 */
public abstract class ResourceLoader {

	public static final ResourceLoader NULL = new ResourceLoader() {
		@Override
		public Stream<URL> getResources(String resourceName) {
			return Stream.of();
		}

		@Override
		public String toString() {
			return "ResourceLoader.NULL";
		}
	};

	public final URL getResource(String resourceName) {
		return getResources(resourceName).findAny().orElse(null);
	}
	protected abstract Stream<URL> getResources(String resourceName);
	
	public final InputStream getResourceAsStream(String name) {
		URL url = getResource(name);
		try {
			return url != null ? url.openStream() : null;
		} catch (IOException e) {
			return null;
		}
		
	}
}
