/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.apache.maven.project.MavenProject;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.springframework.ide.vscode.commons.protocol.java.Gav;
import org.springframework.ide.vscode.commons.protocol.java.ProjectGavParams;
import org.springframework.tooling.gradle.StsGradleToolingModelBuilder;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.resources.ResourceUtils;

@SuppressWarnings("restriction")
public class BuildInfo {
	
	public static CompletableFuture<List<Gav>> projectGAV(ProjectGavParams params, Executor executor, Logger logger) {
		List<Gav> gavs = new ArrayList<>(params.projectUris().size());
		for (int i = 0; i < params.projectUris().size(); i++) {
			// Important to init with nulls all of them such that only successful futures would update values at corresponding indices
			gavs.add(null);
		}
		List<CompletableFuture<?>> f = new ArrayList<>(params.projectUris().size());
		for (int i = 0; i < params.projectUris().size(); i++) {
			final int index = i;
			try {
				IProject project = ResourceUtils.getProject(URI.create(params.projectUris().get(index)));
				if (project != null) {
					f.add(BuildInfo.computeProjectGav(project, executor, logger).thenAccept(gav -> gavs.set(index, gav)));
				}
			} catch (Exception e) {
				// ignore - all pre-filled with 'null' values
			}
		}
		return CompletableFuture.allOf(f.toArray(new CompletableFuture<?>[f.size()])).thenApply(v -> gavs);
	}

	
	private static CompletableFuture<Gav> computeProjectGav(IProject project, Executor executor, Logger logger) {
		try {
			boolean likelyGradle = false;
			boolean likelyMaven = false;
			final Path home = System.getProperty("user.home") == null ? null : new File(System.getProperty("user.home")).toPath();
			if (project.isAccessible() && project.exists()) {
				if (MavenPlugin.isMavenProject(project)) {
					IMavenProjectFacade facade = MavenPlugin.getMavenProjectRegistry().getProject(project);
					if (facade != null) {
						ArtifactKey artifact = facade.getArtifactKey();
						return CompletableFuture.completedFuture(new Gav(artifact.groupId(), artifact.artifactId(), artifact.version()));
					} else {
						return CompletableFuture.supplyAsync(() -> getMavenGav(project.getFile("pom.xml").getFullPath().toFile(), logger), executor) ;
					}
				} else if (GradleProjectNature.isPresentOn(project)) {
					return CompletableFuture.supplyAsync(() -> getGradleBuild(project, logger), executor);
				} else {
					IJavaProject jp = JavaCore.create(project);
					if (jp != null) {
						try {
							for (IClasspathEntry e : jp.getRawClasspath()) {
								if (home != null && e.getPath() != null && e.getPath().toFile() != null) {
									Path path = e.getPath().toFile().toPath();
									if (path.startsWith(home.resolve(".gradle"))) {
										likelyGradle = true;
									} else if (path.startsWith(home.resolve(".m2"))) {
										likelyMaven = true;
									}
								}
							}
						} catch (Exception e) {
							// ignore
						}
					}
				}
			}
			if (likelyMaven) {
				return CompletableFuture.supplyAsync(() -> getMavenGav(project.getFile("pom.xml").getFullPath().toFile(), logger), executor);
			} else if (likelyGradle) {
				return CompletableFuture.supplyAsync(() -> getGradleBuild(project, logger), executor);
			}
		} catch (Exception e) {
			logger.log(e);
		}
		return CompletableFuture.completedFuture(null);
	}
	
	private static Gav getMavenGav(File pom, Logger logger) {
		if (pom.exists()) {
			long start = System.currentTimeMillis();
			try {
				MavenProject mavenProject = MavenPlugin.getMaven().readProject(pom, new NullProgressMonitor());
				return new Gav(mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion());
			} catch (CoreException e) {
				logger.log(e);
			} finally {
				logger.log("Maven GAV project '%s' took %d".formatted(pom.getParentFile().getName(), System.currentTimeMillis() - start));
			}
		} else {
			logger.log("%s does not exists!".formatted(pom.toString()));
		}
		return null;
	}
	
	private static Gav getGradleBuild(IProject project, Logger logger) {
		IFile g = project.getFile("build.gradle");
		if (!g.exists()) {
			g = project.getFile("build.gradle.kts");
		}
		long start = System.currentTimeMillis();
		try {
			return GradleCore.getWorkspace().getBuild(project).flatMap(build -> {
				try {
					return Optional.of(build.withConnection(conn -> StsGradleToolingModelBuilder.getModelBuilder(conn, project.getLocation().toFile(), null).get(), new NullProgressMonitor()));
				} catch (Exception e) {
					logger.log(e);
					return Optional.empty();
				}
			}).map(m -> new Gav(m.group(), m.artifact(), m.version())).orElse(null);
		} finally {
			logger.log("Gradle GAV project '%s' took %d".formatted(project.getName(), System.currentTimeMillis() - start));
		}
	}

}
