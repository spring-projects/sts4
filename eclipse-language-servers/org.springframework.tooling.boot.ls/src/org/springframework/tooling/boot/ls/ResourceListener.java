/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import java.net.URI;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Resource listener for LSP4E taken from CPP-LS
 * https://github.com/eclipse/cdt/blob/master/lsp4e-cpp/org.eclipse.lsp4e.cpp.language/src/org/eclipse/lsp4e/cpp/language/CPPResourceChangeListener.java
 * 
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class ResourceListener implements IResourceChangeListener {
	
	private final LanguageServer server;
	private List<PathMatcher> pathMatchers;

	ResourceListener(LanguageServer server, List<PathMatcher> pathMatchers) {
		this.server = server;
		this.pathMatchers = pathMatchers;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() != IResourceChangeEvent.POST_CHANGE || !isRelevantDelta(event.getDelta())) {
			return;
		}

		sendFileEvents(createFileEventsFromResourceEvent(event));
	}

	private void sendFileEvents(List<FileEvent> fileEvents) {
		if (!fileEvents.isEmpty()) {
			DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(fileEvents);
			server.getWorkspaceService().didChangeWatchedFiles(params);
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

	private static boolean isRelevantDelta(IResourceDelta delta) {
		int kind = delta.getKind();
		int flags = delta.getFlags();
		if (delta.getResource() instanceof IFile && kind == IResourceDelta.CHANGED) {
			return (flags & IResourceDelta.CONTENT) != 0;
		}

		return kind == IResourceDelta.ADDED || kind == IResourceDelta.CHANGED || kind == IResourceDelta.REMOVED;
	}

	private static FileEvent createFileEventFromDelta(IResourceDelta delta) {
		URI locationURI = LSPEclipseUtils.toUri(delta.getResource());
		
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
