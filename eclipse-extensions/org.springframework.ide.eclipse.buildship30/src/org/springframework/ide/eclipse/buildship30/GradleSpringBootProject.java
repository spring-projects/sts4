/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.buildship30;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.buildship.core.internal.workspace.SynchronizationJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectNature;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.MavenId;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrl;
import org.springframework.ide.eclipse.boot.core.internal.SpringBootProject;
import org.springframework.ide.eclipse.boot.util.DependencyDelta;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import sts.model.plugin.StsToolingModel;

@SuppressWarnings("restriction")
public class GradleSpringBootProject extends SpringBootProject {

	private static final String SPRING_BOOT_GROUP = "org.springframework.boot";
	private static final String SPRING_BOOT_NAME = "spring-boot";

	private static final String WAR_PKG_NATURE = "org.eclipse.wst.common.project.facet.core.nature";

	private static final Supplier<Path> cacheFolderSupplier = Suppliers.memoize(() -> {
		IPath iPath = Buildship30Plugin.getDefault().getStateLocation().makeAbsolute();
		return iPath.toFile().toPath().resolve("gradle-plugin");
	});

	private static class GradleModels {
		EclipseProject eclipseProject;
		StsToolingModel stsToolingModel;

		public GradleModels(EclipseProject eclipseProject, StsToolingModel stsToolingModel) {
			super();
			this.eclipseProject = eclipseProject;
			this.stsToolingModel = stsToolingModel;
		}

		public String groupId() {
			return stsToolingModel.group();
		}

		public String bootVersion() {
			for (EclipseExternalDependency dep : eclipseProject.getClasspath()) {
				GradleModuleVersion moduleVersion = dep.getGradleModuleVersion();
				if (SPRING_BOOT_GROUP.equals(moduleVersion.getGroup())
						&& SPRING_BOOT_NAME.equals(moduleVersion.getName())) {
					return moduleVersion.getVersion();
				}
			}
			return null;
		}

		public String version() {
			return stsToolingModel.version();
		}

		public String description() {
			return eclipseProject.getDescription();
		}

		public String artifactId() {
			return stsToolingModel.artifact();
		}

		public String packaging() {
			for (EclipseProjectNature nature : eclipseProject.getProjectNatures()) {
				if (WAR_PKG_NATURE.equals(nature.getId())) {
					return "war";
				}
			}
			return "jar";
		}

		public String javaVersion() {
			return eclipseProject.getJavaSourceSettings().getSourceLanguageLevel().getMajorVersion();
		}
	}

	private Supplier<GradleModels> gradleProject = Suppliers.memoize(() -> {
		try {
			return CompletableFuture.supplyAsync(() -> {
				Optional<GradleBuild> build = GradleCore.getWorkspace().getBuild(project);
				if (build.isPresent()) {
					try {
						return build.get().withConnection(connection -> {
							ModelBuilder<StsToolingModel> stsModelBuilder = connection.model(StsToolingModel.class)
									.withArguments("--init-script", getInitScript().toString());
							return new GradleModels(connection.model(EclipseProject.class).get(),
									stsModelBuilder.get());
						}, new NullProgressMonitor());
					} catch (Exception e) {
						Log.log(e);
						return null;
					}
				}
				Log.error("Cannot connect to Gradle project");
				return null;
			}).get();
		} catch (Exception e) {
			return null;
		}
	});

	public GradleSpringBootProject(IProject project, InitializrService initializr) {
		super(project, initializr);
	}

	@Override
	public List<IMavenCoordinates> getDependencies() throws Exception {
		return null;
	}

	@Override
	public void modifyDependencies(DependencyDelta values) throws Exception {
		throw new UnsupportedOperationException("Not implemented!");
	}

	@Override
	public void addMavenDependency(IMavenCoordinates dep, boolean preferManagedVersion) throws Exception {
		throw new UnsupportedOperationException("Not implemented!");
	}

	@Override
	public void addMavenDependency(IMavenCoordinates depConfigurationProcessor, boolean preferManagedVersion,
			boolean optional) throws CoreException {
		throw new UnsupportedOperationException("Not implemented!");
	}

	@Override
	public String getBootVersion() {
		GradleModels models = gradleProject.get();
		if (models != null) {
			return models.bootVersion();
		}
		return null;
	}

	@Override
	public Job updateProjectConfiguration() {
		return GradleCore.getWorkspace().getBuild(project).map(build -> {
			SynchronizationJob job = new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE,
					Collections.singleton(build));
			job.schedule();
			return job;
		}).orElse(null);
	}

	@Override
	public void removeMavenDependency(MavenId mavenId) throws CoreException {
		throw new UnsupportedOperationException("Not implemented!");
	}

	@Override
	public String getDependencyFileName() {
		throw new UnsupportedOperationException("Not implemented!");
	}

	@Override
	public String getPackaging() throws CoreException {
		GradleModels models = gradleProject.get();
		if (models != null) {
			return models.packaging();
		}
		return null;
	}

	@Override
	public String buildType() {
		return InitializrUrl.GRADLE_PROJECT;
	}

	@Override
	public String artifactId() throws CoreException {
		GradleModels models = gradleProject.get();
		if (models != null) {
			return models.artifactId();
		}
		return null;
	}

	@Override
	public String groupId() throws CoreException {
		GradleModels models = gradleProject.get();
		if (models != null) {
			return models.groupId();
		}
		return null;
	}

	@Override
	public String javaVersion() throws CoreException {
		GradleModels models = gradleProject.get();
		if (models != null) {
			return models.javaVersion();
		}
		return null;
	}

	@Override
	public String version() throws CoreException {
		GradleModels models = gradleProject.get();
		if (models != null) {
			return models.version();
		}
		return null;
	}

	@Override
	public String description() throws CoreException {
		GradleModels models = gradleProject.get();
		if (models != null) {
			return models.description();
		}
		return null;
	}

	@Override
	public File executePackagingScript(IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("Not implemented!");
	}

	private Path getInitScript() {
		Path cacheFolder = cacheFolderSupplier.get();
		Path initScript = cacheFolder.resolve("init.gradle");
		Path jarPath = cacheFolder.resolve("sts-gradle-model-plugin.jar");
		if (!Files.isDirectory(cacheFolder)) {
			cacheFolder.toFile().mkdirs();
		}
		if (!Files.exists(initScript)) {
			try {
				Files.copy(getClass().getResourceAsStream("/gradle-plugin/init.gradle"), initScript);
			} catch (IOException e) {
				Log.log(e);
			}
		}
		if (!Files.exists(jarPath)) {
			try {
				Files.copy(getClass().getResourceAsStream("/gradle-plugin/sts-gradle-model-plugin.jar"),
						jarPath);
			} catch (IOException e) {
				Log.log(e);
			}
		}
		return initScript;
	}

}
