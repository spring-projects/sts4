/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.transformer.JavaScopeDeriver;
import org.eclipse.aether.util.graph.transformer.JavaScopeSelector;
import org.eclipse.aether.util.graph.transformer.NearestVersionSelector;
import org.eclipse.aether.util.graph.transformer.SimpleOptionalitySelector;
import org.eclipse.aether.util.graph.visitor.CloningDependencyVisitor;
import org.eclipse.aether.util.graph.visitor.FilteringDependencyVisitor;

/**
 * Maven Core functionality
 * 
 * @author Alex Boyko
 *
 */
public class MavenCore {
	
	private static MavenCore instance = null;
	
	private MavenBridge maven = new MavenBridge();
	
	public static MavenCore getInstance() {
		if (instance == null) {
			instance = new MavenCore();
		}
		return instance;
	}
	
	/**
	 * Creates Maven Project descriptor based on the pom file.
	 * 
	 * @param pom The pom file
	 * @return Maven project instance
	 * @throws MavenException
	 */
	public MavenProject readProject(File pom) throws MavenException {
		return maven.readProject(pom, maven.createExecutionRequest());
	}
	
	/**
	 * Taken from M2E same named method from MavenModelManager
	 * 
	 * @param repositorySystem
	 * @param repositorySession
	 * @param mavenProject
	 * @param scope
	 * @return
	 */
	private DependencyNode readDependencyTree(org.eclipse.aether.RepositorySystem repositorySystem,
			RepositorySystemSession repositorySession, MavenProject mavenProject, String scope) {
		DefaultRepositorySystemSession session = new DefaultRepositorySystemSession(repositorySession);

		ConflictResolver transformer = new ConflictResolver(new NearestVersionSelector(), new JavaScopeSelector(),
				new SimpleOptionalitySelector(), new JavaScopeDeriver());
		session.setDependencyGraphTransformer(transformer);
		session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, Boolean.toString(true));
		session.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true);

		ArtifactTypeRegistry stereotypes = session.getArtifactTypeRegistry();

		CollectRequest request = new CollectRequest();
		request.setRequestContext("project"); //$NON-NLS-1$
		request.setRepositories(mavenProject.getRemoteProjectRepositories());

		for (org.apache.maven.model.Dependency dependency : mavenProject.getDependencies()) {
			request.addDependency(RepositoryUtils.toDependency(dependency, stereotypes));
		}

		DependencyManagement depMngt = mavenProject.getDependencyManagement();
		if (depMngt != null) {
			for (org.apache.maven.model.Dependency dependency : depMngt.getDependencies()) {
				request.addManagedDependency(RepositoryUtils.toDependency(dependency, stereotypes));
			}
		}

		DependencyNode node;
		try {
			node = repositorySystem.collectDependencies(session, request).getRoot();
		} catch (DependencyCollectionException ex) {
			node = ex.getResult().getRoot();
		}

		Collection<String> scopes = new HashSet<String>();
		Collections.addAll(scopes, Artifact.SCOPE_SYSTEM, Artifact.SCOPE_COMPILE, Artifact.SCOPE_PROVIDED,
				Artifact.SCOPE_RUNTIME, Artifact.SCOPE_TEST);
		if (Artifact.SCOPE_COMPILE.equals(scope)) {
			scopes.remove(Artifact.SCOPE_COMPILE);
			scopes.remove(Artifact.SCOPE_SYSTEM);
			scopes.remove(Artifact.SCOPE_PROVIDED);
		} else if (Artifact.SCOPE_RUNTIME.equals(scope)) {
			scopes.remove(Artifact.SCOPE_COMPILE);
			scopes.remove(Artifact.SCOPE_RUNTIME);
		} else if (Artifact.SCOPE_COMPILE_PLUS_RUNTIME.equals(scope)) {
			scopes.remove(Artifact.SCOPE_COMPILE);
			scopes.remove(Artifact.SCOPE_SYSTEM);
			scopes.remove(Artifact.SCOPE_PROVIDED);
			scopes.remove(Artifact.SCOPE_RUNTIME);
		} else {
			scopes.clear();
		}

		CloningDependencyVisitor cloner = new CloningDependencyVisitor();
		node.accept(new FilteringDependencyVisitor(cloner, new ScopeDependencyFilter(null, scopes)));
		node = cloner.getRootNode();

		return node;
	}
	
	/**
	 * Calculates dependency graph for a Maven project provided the scope.
	 * 
	 * @param project Maven Project descriptor
	 * @param scope Dependency scope
	 * @return Set of all dependencies including transient ones
	 * @throws MavenException
	 */
	public Set<Artifact> resolveDependencies(MavenProject project, String scope) throws MavenException {
		Set<Artifact> artifacts = new LinkedHashSet<>();

		MavenExecutionRequest request = maven.createExecutionRequest();
		DefaultRepositorySystemSession session = maven.createRepositorySession(request);

		DependencyNode graph = readDependencyTree(maven.lookupComponent(org.eclipse.aether.RepositorySystem.class), session, project, scope);
		if (graph != null) {
			RepositoryUtils.toArtifacts(artifacts, graph.getChildren(),
					Collections.singletonList(project.getArtifact().getId()), null);

			// Maven 2.x quirk: an artifact always points at the local repo,
			// regardless whether resolved or not
			LocalRepositoryManager lrm = session.getLocalRepositoryManager();
			for (Artifact artifact : artifacts) {
				if (!artifact.isResolved()) {
					String path = lrm.getPathForLocalArtifact(RepositoryUtils.toArtifact(artifact));
					artifact.setFile(new File(lrm.getRepository().getBasedir(), path));
				}
			}
		}
		
		return artifacts;
	}
}
