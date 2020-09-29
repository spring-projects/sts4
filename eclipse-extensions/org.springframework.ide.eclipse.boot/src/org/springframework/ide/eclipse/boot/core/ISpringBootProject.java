/*******************************************************************************
 * Copyright (c) 2013, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.util.DependencyDelta;

/**
 * SpringBoot-centric view on an IProject instance.
 *
 * @author Kris De Volder
 */
public interface ISpringBootProject {

	final static String PACKAGING_JAR = "jar";
	final static String PACKAGING_WAR = "war";

	/**
	 * @return corresponding Eclipse project.
	 */
	public IProject getProject();

	/**
	 * Fetches list of dependencies from the project. Dependencies returned may have 'incomplete' coordinates.
	 * For example may only have group-id and artifact-id but not version. The info is extracted
	 * from something like a pom.xml and the info that is listed there may also be incomplete.
	 * @throws CoreException
	 */
	public List<IMavenCoordinates> getDependencies() throws Exception;

	/**
	 * @return Infos about the known spring boot starters. These are 'discovered' by querying
	 * the initializr web service.
	 *
	 * TODO: if we have this, do we still need 'getKnownStarters' method?
	 */
	public SpringBootStarters getStarterInfos() throws Exception;

	/**
	 * @return Infos about the known spring boot starters. These are 'discovered' by querying
	 * the initializr web service.
	 *
	 * @throws CoreException
	 */
	public List<SpringBootStarter> getKnownStarters() throws Exception;

	/**
	 * Gets a list of bootstarters that are currently applied to the project.
	 * @throws CoreException
	 * @throws Exception
	 */
	public List<SpringBootStarter> getBootStarters() throws Exception;

	/**
	 * Modify project classpath adding and/or removing maven dependencies
	 */
	public void modifyDependencies(DependencyDelta values) throws Exception;

	/**
	 * Modify project's classpath to add a given maven style dependency.
	 * The way this dependency is added may depend on the type of project. E.g.
	 * for a maven project it will be added to the project's pom file in the
	 * dependencies section.
	 */
	public void addMavenDependency(IMavenCoordinates dep, boolean preferManagedVersion) throws Exception;

	/**
	 * @since 3.7.0
	 */
	public void addMavenDependency(IMavenCoordinates depConfigurationProcessor, boolean preferManagedVersion, boolean optional) throws CoreException;


	/**
	 * Version of spring boot on this project's classpath. (This is determined by looking for artifact with id "spring-boot".
	 * The base version of that artifact will then be used.
	 */
	public String getBootVersion();

	/**
	 * Equivalent of triggering a 'update project' operation on a Maven project. I.e. re-apply whatever configuration
	 * gets done based on pom.xml or its equivalent. Client calling this should beware that this operation may be
	 * asynchronous.
	 * @return Job if the operation is asynchronous or null otherwise.
	 */
	Job updateProjectConfiguration();

	/**
	 * Remove a dependency with given group-id and artifact-id from project's pom or build script.
	 */
	public void removeMavenDependency(MavenId mavenId) throws CoreException;

	/**
	 * The name of the file in which the list of dependencies for this project are maintained. E.g "pom.xml" for maven projects.
	 */
	public String getDependencyFileName();

	public String getPackaging() throws CoreException;

	public String buildType();

	public String artifactId() throws CoreException;

	public String groupId() throws CoreException;

	public String javaVersion() throws CoreException;

	public String version() throws CoreException;

	public String description() throws CoreException;

	public String packageName() throws CoreException;

	public File executePackagingScript(IProgressMonitor monitor) throws CoreException;

	/**
	 * Generates the contetns of a pom using this project's initializer service, using given list of dependencies
	 * to select known starters (i.e. any dependency in the list that corresponds to a known starter will be selected.
	 * Dependencies not corresponding to a known starter are silently ignored.
	 */
	public String generatePom(List<Dependency> initialDependencies) throws Exception;

}
