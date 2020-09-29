/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import reactor.core.publisher.Flux;

/**
 * Subset of {@link ClassLoader} interface. Mimicks behavior of
 * classloaders but only can be used to load 'resources' as data.
 * 
 * @author Kris De Volder
 */
public abstract class ResourceLoader {

	public static final ResourceLoader NULL = new ResourceLoader() {
		@Override
		public Flux<URL> getResources(String resourceName) {
			return Flux.empty();
		}

		@Override
		public String toString() {
			return "ResourceLoader.NULL";
		}
	};

	public final URL getResource(String resourceName) {
		return getResources(resourceName).blockFirst();
	}
	protected abstract Flux<URL> getResources(String resourceName);
	
	public final InputStream getResourceAsStream(String name) {
		URL url = getResource(name);
		try {
			return url != null ? url.openStream() : null;
		} catch (IOException e) {
			return null;
		}
		
	}
}
