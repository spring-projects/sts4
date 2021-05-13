package org.springframework.ide.vscode.commons.languageserver.java;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;

public class FutureProjectFinder implements DisposableBean {
	
	private JavaProjectFinder projectFinder;
	private ProjectObserver projectObserver;
	
	private WeakHashMap<URI, CompletableFuture<IJavaProject>> pendingFindProjectRequests = new WeakHashMap<>();
	
	private final Listener LISTENER = new Listener() {
		
		@Override
		public void deleted(IJavaProject project) {
		}
		
		@Override
		public void created(IJavaProject project) {
			resolvePendingRequests(project);
		}
		
		@Override
		public void changed(IJavaProject project) {
		}
	};
	
	
	public FutureProjectFinder(JavaProjectFinder projectFinder, Optional<ProjectObserver> projectObserver) {
		this.projectFinder = projectFinder;
		this.projectObserver = projectObserver.orElse(null);
		if (this.projectObserver != null) {
			this.projectObserver.addListener(LISTENER);
		}
	}

	synchronized private void resolvePendingRequests(IJavaProject project) {
		for (Map.Entry<URI, CompletableFuture<IJavaProject>> e : pendingFindProjectRequests.entrySet()) {
			Optional<IJavaProject> jp = projectFinder.find(new TextDocumentIdentifier(e.getKey().toString()));
			if (jp.isPresent()) {
				e.getValue().complete(jp.get());
				pendingFindProjectRequests.remove(e.getKey());
			}
		}
	}

	@Override
	public void destroy() throws Exception {
		if (projectObserver != null) {
			projectObserver.removeListener(LISTENER);
		}
	}
	
	synchronized public CompletableFuture<IJavaProject> findFuture(URI uri) {		
		TextDocumentIdentifier id = new TextDocumentIdentifier(uri.toString());
		Optional<IJavaProject> jp = projectFinder.find(id);
		if (jp.isPresent()) {
			return CompletableFuture.completedFuture(jp.get());
		} else {
			if (projectObserver == null) {
				throw new IllegalStateException("Future project lookup not supported without ProjectObserver bean present");
			}
			CompletableFuture<IJavaProject> cf = pendingFindProjectRequests.get(uri);
			if (cf == null) {
				cf = new CompletableFuture<IJavaProject>();
				pendingFindProjectRequests.put(uri, cf);
			}
			return cf;
		}
	}


}
