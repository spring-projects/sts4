/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.initializr;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.maven.shared.utils.io.IOUtil;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.core.SimpleUriBuilder;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;

public interface InitializrService {

	public static final InitializrService DEFAULT = create(BootActivator.getUrlConnectionFactory(), BootPreferences::getDefaultInitializrUrl);

	SpringBootStarters getStarters(String bootVersion) throws Exception;

	/**
	 * Generates a pom by contacting intializer service.
	 */
	String getPom(Map<String, ?> parameters) throws Exception;

	static InitializrService create(URLConnectionFactory urlConnectionFactory, Supplier<String> baseUrl) {
		return new InitializrService() {
			@Override
			public SpringBootStarters getStarters(String bootVersion) throws Exception {
				URL initializrUrl = new URL(baseUrl.get());
				URL dependencyUrl = dependencyUrl(bootVersion, initializrUrl);
				return SpringBootStarters.load(
						initializrUrl, dependencyUrl,
						BootActivator.getUrlConnectionFactory()
				);
			}

			private URL dependencyUrl(String bootVersion, URL initializerUrl) throws MalformedURLException {
				SimpleUriBuilder builder = new SimpleUriBuilder(initializerUrl.toString()+"/dependencies");
				builder.addParameter("bootVersion", bootVersion);
				return new URL(builder.toString());
			}

			@Override
			public String getPom(Map<String, ?> parameters) throws Exception {
				//Example uri:
				//https://start-development.cfapps.io/starter.zip
				//	?name=demo&groupId=com.example&artifactId=demo
				//  &version=0.0.1-SNAPSHOT
				//  &description=Demo+project+for+Spring+Boot&packageName=com.example.demo
				//  &type=maven-project
				//  &packaging=jar
				//  &javaVersion=1.8
				//  &language=java
				//  &bootVersion=2.0.3.RELEASE
				//  &dependencies=cloud-aws&dependencies=cloud-hystrix-dashboard&dependencies=web

				SimpleUriBuilder builder = new SimpleUriBuilder(baseUrl.get()+"/pom.xml");
				for (Map.Entry<String, ?> entry : parameters.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					if (value instanceof String) {
						builder.addParameter(key, (String) value);
					} else if (value instanceof Collection) {
						for (Object item : (Collection<?>) value) {
							if (item instanceof String) {
								builder.addParameter(key, (String) item);
							}
						}
					}
				}
				URLConnection urlConnection = urlConnectionFactory.createConnection(new URL(builder.toString()));
				urlConnection.connect();
				return IOUtil.toString(urlConnection.getInputStream(), "UTF8");
			}
		};
	}

}
