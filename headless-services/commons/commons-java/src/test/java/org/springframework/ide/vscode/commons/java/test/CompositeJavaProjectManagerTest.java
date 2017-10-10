/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.AbstractJavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectManager;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectManager.Listener;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;
import org.springframework.ide.vscode.commons.util.FileObserver.FileListener;
import org.springframework.ide.vscode.commons.util.FileUtils;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Tests for {@link CompositeJavaProjectManager}
 * 
 * @author Alex Boyko
 *
 */
public class CompositeJavaProjectManagerTest {
	
	private AbstractJavaProjectManager createProjectManagerForFile(String fileName) {
		return new AbstractJavaProjectManager() {
			
			private Cache<File, IJavaProject> cache = CacheBuilder.newBuilder().build();
		
			@Override
			public boolean isProjectRoot(File file) {
				return FileUtils.findFile(file, fileName, false) != null;
			}
			
			@Override
			public IJavaProject find(File file) {
				if (fileName.equals(file.toPath().getFileName().toString())) {
					try {
						return cache.get(file, () -> {
							return new IJavaProject() {
								@Override
								public IClasspath getClasspath() {
									return null;
								}
							};
						});
					} catch (ExecutionException e) {
						Log.log(e);
						return null;
					}
				}
				return null;	
			}

			@Override
			protected FileListener createFileListener() {
				return new FileListener() {
					
					private File getFileFromUri(String uri) {
						return Paths.get(URI.create(uri)).toFile();
					}
					
					@Override
					public void deleted(String uri) {
						File file = getFileFromUri(uri);
						IJavaProject project = cache.getIfPresent(file);
						if (project != null) {
							cache.invalidate(file);
							notifyProjectDeleted(project);
						}
					}
					
					@Override
					public void created(String uri) {
					}
					
					@Override
					public void changed(String uri) {
						File file = getFileFromUri(uri);
						IJavaProject project = cache.getIfPresent(file);
						if (project != null) {
							notifyProjectChanged(project);
						}
					}
					
					@Override
					public boolean accept(String uri) {
						return fileName.equals(Paths.get(URI.create(uri)).getFileName().toString());
					}
				};
			}
			
			
		};
	}
	
	@Test
	public void testListeners() throws Exception {
		AbstractJavaProjectManager manager1 = createProjectManagerForFile("test-1");
		AbstractJavaProjectManager manager2 = createProjectManagerForFile("test-2");
		CompositeJavaProjectManager compositeManager = new CompositeJavaProjectManager(new JavaProjectManager[] {
			manager1,
			manager2
		});
		BasicFileObserver fileObserver = new BasicFileObserver();
		compositeManager.setFileObserver(fileObserver);
		
		Path containerPath = Paths.get(CompositeJavaProjectManagerTest.class.getResource("/").toURI());
		
		File projectFile1 = containerPath.resolve("test-1").toFile();
		IJavaProject p1 = compositeManager.find(projectFile1);
		assertNotNull(p1);
		
		File projectFile2 = containerPath.resolve("test-2").toFile();
		IJavaProject p2 = compositeManager.find(projectFile2);
		assertNotNull(p2);
		
		IJavaProject[] projectChanged = new IJavaProject[] { null };
		IJavaProject[] projectDeleted = new IJavaProject[] { null };

		compositeManager.addListener(new Listener() {

			@Override
			public void created(IJavaProject project) {
			}

			@Override
			public void changed(IJavaProject project) {
				projectChanged[0] = project;
			}

			@Override
			public void deleted(IJavaProject project) {
				projectDeleted[0] = project;
			}
			
		});
		
		fileObserver.notifyFileChanged(projectFile1.toURI().toString());
		assertEquals(p1, projectChanged[0]);
		assertNull(projectDeleted[0]);				
		projectChanged[0] = projectDeleted[0] = null;
		
		fileObserver.notifyFileDeleted(projectFile1.toURI().toString());
		assertNull(projectChanged[0]);
		assertEquals(p1, projectDeleted[0]);				
		projectChanged[0] = projectDeleted[0] = null;
		
		fileObserver.notifyFileChanged(projectFile2.toURI().toString());
		assertEquals(p2, projectChanged[0]);
		assertNull(projectDeleted[0]);				
		projectChanged[0] = projectDeleted[0] = null;
		
		fileObserver.notifyFileDeleted(projectFile2.toURI().toString());
		assertNull(projectChanged[0]);
		assertEquals(p2, projectDeleted[0]);				
		projectChanged[0] = projectDeleted[0] = null;
	}

}
