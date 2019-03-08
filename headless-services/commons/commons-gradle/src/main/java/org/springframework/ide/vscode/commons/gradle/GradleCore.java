/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.gradle;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.internal.consumer.DefaultGradleConnector;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.Log;

/**
 * Gradle API tooling utility
 * 
 * @author Alex Boyko
 *
 */
public class GradleCore {
	
	@FunctionalInterface
	public interface GradleConfiguration {
		void configure(GradleConnector connector);
	}
	
	public interface GradleCoreProject {
		EclipseProject getProject();
		BuildEnvironment getBuildEnvironment();
	}
	
	static final String GRADLE_BUILD_FILE = "build.gradle";

	static final String GLOB_GRADLE_FILE = "**/*.gradle";
	
	private static GradleCore defaultInstance = null;
	
	public static GradleCore getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new GradleCore();
		}
		return defaultInstance;
	}
	
	private GradleConfiguration configuration;
	
	public GradleCore() {
		this.configuration = (connector) -> {};
	}
	
	public GradleCore(GradleConfiguration configuration) {
		Assert.isNotNull(configuration);
		this.configuration = configuration;
	}
	
	public <T> T getModel(File projectDir, Class<T> modelType) throws GradleException {
		ProjectConnection connection = null;
		try {
			GradleConnector gradleConnector = GradleConnector.newConnector().forProjectDirectory(projectDir);
			/*
			 * Shut down Gradle daemons right away. Necessary project data is
			 * queried once and then cached, hence no need need to have the
			 * daemon running
			 */
			((DefaultGradleConnector) gradleConnector).daemonMaxIdleTime(1, TimeUnit.SECONDS);
			configuration.configure(gradleConnector);
			// Use patched Gradle 4.4 distribution or higher as a workaround for https://github.com/gradle/gradle/issues/2483
			gradleConnector.useGradleVersion("4.6");
			connection = gradleConnector.connect();
			return connection.getModel(modelType);
		} catch (GradleConnectionException e) {
			Log.log(e);
			throw new GradleException(e);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
}
