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
package org.springframework.tooling.boot.java.ls;

import java.io.IOException;
import java.net.URI;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.ProjectSpecificLanguageServerWrapper;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;

/**
 * Resource listener for LSP4E taken from CPP-LS
 * https://github.com/eclipse/cdt/blob/master/lsp4e-cpp/org.eclipse.lsp4e.cpp.language/src/org/eclipse/lsp4e/cpp/language/CPPResourceChangeListener.java
 * 
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class ResourceListener implements IResourceChangeListener {
	
	private final String lsId;
	private final IProject fProject;
	private List<PathMatcher> pathMatchers;

	ResourceListener(String lsId, IProject project, List<PathMatcher> pathMatchers) {
		this.lsId = lsId;
		fProject = project;
		this.pathMatchers = pathMatchers;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		LanguageServerDefinition definition = LanguageServersRegistry.getInstance().getDefinition(lsId);
		ProjectSpecificLanguageServerWrapper wrapper = getLanguageSeverWrapper(definition);
		if (event.getType() != IResourceChangeEvent.POST_CHANGE || !isRelevantDelta(event.getDelta())
				|| wrapper == null) {
			return;
		}

		sendFileEvents(wrapper, createFileEventsFromResourceEvent(event));
	}

	private static void sendFileEvents(ProjectSpecificLanguageServerWrapper wrapper, List<FileEvent> fileEvents) {
		if (!fileEvents.isEmpty()) {
			DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(fileEvents);
			wrapper.getServer().getWorkspaceService().didChangeWatchedFiles(params);
		}
	}

	private List<FileEvent> createFileEventsFromResourceEvent(IResourceChangeEvent event) {
		List<FileEvent> fileEvents = new ArrayList<>();
		try {
			event.getDelta().accept((delta) -> {
				if (delta.getResource() instanceof IFile && isRelevantDelta(delta) && isApplicableFile((IFile) delta.getResource())) {
					FileEvent fileEvent = createFileEventFromDelta(delta);
					if (fileEvent != null) {
						fileEvents.add(fileEvent);
					}
				}
				return true;
			}, false);
		} catch (CoreException e) {
			// Do nothing
		}
		return fileEvents;
	}

	private boolean isApplicableFile(IFile resource) {
		return pathMatchers.stream().filter(m -> m.matches(resource.getLocation().toFile().toPath())).findFirst().isPresent();
	}

	private ProjectSpecificLanguageServerWrapper getLanguageSeverWrapper(LanguageServerDefinition definition) {
		try {
			return LanguageServiceAccessor.getLSWrapperForConnection(fProject, definition);
		} catch (IOException e) {
			// Do nothing
			return null;
		}
	}

	private static boolean isRelevantDelta(IResourceDelta delta) {
		int kind = delta.getKind();
		int flags = delta.getFlags();
		if (delta.getResource() instanceof IFile && kind == IResourceDelta.CHANGED) {
			return (flags & IResourceDelta.CONTENT) != 0;
		}

		return kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED || kind == IResourceDelta.REMOVED;
	}

	private static FileEvent createFileEventFromDelta(IResourceDelta delta) {
		URI locationURI = delta.getResource().getLocationURI();
		if (locationURI == null) {
			return null;
		}

		FileChangeType changeType = null;
		if (delta.getKind() == IResourceDelta.ADDED) {
			changeType = FileChangeType.Created;
		} else if (delta.getKind() == IResourceDelta.CHANGED) {
			changeType = FileChangeType.Changed;
		} else if (delta.getKind() == IResourceDelta.REMOVED) {
			changeType = FileChangeType.Deleted;
		} else {
			throw new IllegalStateException("Unsupported resource delta kind: " + delta.getKind()); //$NON-NLS-1$
		}

		return new FileEvent(locationURI.toString(), changeType);
	}
}
