/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionResolver;
import org.springsource.ide.eclipse.commons.core.JdtUtils;

/**
 * @author Christian Dupuis
 */
public class ProjectClasspathNamespaceDefinitionResolverCache {

	private static final int CACHE_SIZE = 12;

	private static List<ResolvlerCacheEntry> RESOLVER_CACHE = new ArrayList<ResolvlerCacheEntry>(CACHE_SIZE);

	private static INamespaceDefinitionResolver addResolverToCache(IProject project) {
		synchronized (RESOLVER_CACHE) {
			int nEntries = RESOLVER_CACHE.size();
			if (nEntries >= CACHE_SIZE) {
				// find obsolete entries or remove entry that was least recently accessed
				ResolvlerCacheEntry oldest = null;
				List<ResolvlerCacheEntry> obsoleteClassLoaders = new ArrayList<ResolvlerCacheEntry>(CACHE_SIZE);
				for (int i = 0; i < nEntries; i++) {
					ResolvlerCacheEntry entry = RESOLVER_CACHE.get(i);
					IProject curr = entry.getProject();
					if (!curr.exists() || !curr.isAccessible() || !curr.isOpen()) {
						obsoleteClassLoaders.add(entry);
					}
					else {
						if (oldest == null || entry.getLastAccess() < oldest.getLastAccess()) {
							oldest = entry;
						}
					}
				}
				if (!obsoleteClassLoaders.isEmpty()) {
					for (int i = 0; i < obsoleteClassLoaders.size(); i++) {
						removeResolverEntryFromCache(obsoleteClassLoaders.get(i));
					}
				}
				else if (oldest != null) {
					removeResolverEntryFromCache(oldest);
				}
			}
			ResolvlerCacheEntry newEntry = new ResolvlerCacheEntry(project);
			RESOLVER_CACHE.add(newEntry);
			return newEntry.getResolver();
		}
	}

	private static INamespaceDefinitionResolver findResolverInCache(IProject project) {
		synchronized (RESOLVER_CACHE) {
			for (int i = RESOLVER_CACHE.size() - 1; i >= 0; i--) {
				ResolvlerCacheEntry entry = RESOLVER_CACHE.get(i);
				IProject curr = entry.getProject();
				if (!curr.exists() || !curr.isAccessible() || !curr.isOpen()) {
					removeResolverEntryFromCache(entry);
				}
				else {
					if (entry.matches(project)) {
						entry.markAsAccessed();
						return entry.getResolver();
					}
				}
			}
		}
		return null;
	}

	private static void removeResolverEntryFromCache(ResolvlerCacheEntry entry) {
		synchronized (RESOLVER_CACHE) {
			entry.dispose();
			RESOLVER_CACHE.remove(entry);
		}
	}

	public synchronized static INamespaceDefinitionResolver getResolver(IProject project) {
		INamespaceDefinitionResolver resolver = findResolverInCache(project);
		if (resolver == null) {
			resolver = addResolverToCache(project);
		}
		return resolver;
	}

	/**
	 * Internal cache entry
	 */
	private static class ResolvlerCacheEntry implements IElementChangedListener {

		private long lastAccess;

		private IProject project;
		
		private ProjectClasspathNamespaceDefinitionResolver resolver;

		public ResolvlerCacheEntry(IProject project) {
			this.project = project;
			this.resolver = new ProjectClasspathNamespaceDefinitionResolver(project);
			markAsAccessed();
			JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
		}

		public void dispose() {
			JavaCore.removeElementChangedListener(this);
			resolver.dispose();
			resolver = null;
		}

		public void elementChanged(ElementChangedEvent event) {
			IJavaProject javaProject = JdtUtils.getJavaProject(project);
			if (javaProject != null) {
				for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
					if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0
							|| (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
						if (javaProject.equals(delta.getElement()) || javaProject.isOnClasspath(delta.getElement())) {
							removeResolverEntryFromCache(this);
						}
					}
				}
			}
		}
		
		public INamespaceDefinitionResolver getResolver() {
			return resolver;
		}
		
		public long getLastAccess() {
			return lastAccess;
		}

		public IProject getProject() {
			return this.project;
		}

		public void markAsAccessed() {
			lastAccess = System.currentTimeMillis();
		}

		public boolean matches(IProject project) {
			return this.project.equals(project);
		}

	}

}
