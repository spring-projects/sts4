/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.gradle;

import java.io.File;

import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.springframework.ide.vscode.commons.util.Assert;

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
	
	public GradleCoreProject readProject(File projectDir) throws GradleException {
		ProjectConnection connection = null;
		try {
			GradleConnector gradleConnector = GradleConnector.newConnector().forProjectDirectory(projectDir);
			configuration.configure(gradleConnector);
			connection = gradleConnector.connect();;
			final EclipseProject project = connection.getModel(EclipseProject.class);
			final BuildEnvironment build = connection.getModel(BuildEnvironment.class);
			return new GradleCoreProject() {
				
				@Override
				public EclipseProject getProject() {
					return project;
				}
				
				@Override
				public BuildEnvironment getBuildEnvironment() {
					return build;
				}
			};
		} catch (GradleConnectionException e) {
			throw new GradleException(e);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	public <T> T getModel(File projectDir, Class<T> modelType) throws GradleException {
		ProjectConnection connection = null;
		try {
			GradleConnector gradleConnector = GradleConnector.newConnector().forProjectDirectory(projectDir);
			configuration.configure(gradleConnector);
			connection = gradleConnector.connect();
			return connection.getModel(modelType);
		} catch (GradleConnectionException e) {
			throw new GradleException(e);
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
}
