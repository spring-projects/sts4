/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.vscode.xml.namespaces.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenient utility methods for loading of {@code java.util.Properties},
 * performing standard handling of input streams.
 *
 * <p>For more configurable properties loading, including the option of a
 * customized encoding, consider using the PropertiesLoaderSupport class.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see PropertiesLoaderSupport
 */
public abstract class PropertiesLoaderUtils {
	
	private static Logger LOGGER = Logger.getLogger(PropertiesLoaderUtils.class.getName());

	private static final String XML_FILE_EXTENSION = ".xml";

	/**
	 * Load all properties from the specified class path resource
	 * (in ISO-8859-1 encoding), using the given class loader.
	 * <p>Merges properties if more than one resource of the same name
	 * found in the class path.
	 * @param resourceName the name of the class path resource
	 * @param classLoader the ClassLoader to use for loading
	 * (or {@code null} to use the default class loader)
	 * @return the populated Properties instance
	 * @throws IOException if loading failed
	 */
	public static Properties loadAllProperties(String resourceName, ResourceLoader classLoader) {
		if (resourceName == null) {
			throw new IllegalArgumentException("Resource name must not be null");
		}
		ResourceLoader classLoaderToUse = classLoader;
		if (classLoaderToUse == null) {
			classLoaderToUse = ResourceLoader.NULL;
		}
		ResourceLoader loader = classLoaderToUse;
		Properties props = new Properties();
		loader.getResources(resourceName).parallel().forEach(url -> {
			try {
				URLConnection con = url.openConnection();
				useCachesIfNecessary(con);
				InputStream is = con.getInputStream();
				try {
					if (resourceName.endsWith(XML_FILE_EXTENSION)) {
						props.loadFromXML(is);
					} else {
						props.load(is);
					}
				} finally {
					is.close();
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e, null);
			}
		});
		return props;
	}
	
	/**
	 * Set the {@link URLConnection#setUseCaches "useCaches"} flag on the
	 * given connection, preferring {@code false} but leaving the
	 * flag at {@code true} for JNLP based resources.
	 * @param con the URLConnection to set the flag on
	 */
	private static void useCachesIfNecessary(URLConnection con) {
		con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
	}

}
