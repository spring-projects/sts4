/*******************************************************************************
 *  Copyright (c) 2013, 2016 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.content;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.boot.wizard.BootWizardActivator;
import org.springframework.ide.eclipse.boot.wizard.github.auth.AuthenticatedDownloader;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.DownloadManager;
import org.springsource.ide.eclipse.commons.frameworks.core.util.JobUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

/**
 * An instance of the class manages lists of content of different types. The
 * idea is to create a subclass that provides all the concrete details on how
 * different types of content are discovered, downloaded and cached on the local
 * file system.
 * <p>
 * But the infrastructure for managing/downloading the content is shared.
 *
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class ContentManager {

	private final Map<Class<?>, TypedContentManager<?>> byClass = new HashMap<>();
	private final List<ContentType<?>> types = new ArrayList<>();

	protected LiveVariable<DownloadState> prefetchContentTracker = new LiveVariable<>(DownloadState.NOT_STARTED);
	protected Throwable prefetchContentError = null;

	protected LiveVariable<DownloadState> prefetchContentProviderPropertiesTracker = new LiveVariable<>(
			DownloadState.NOT_STARTED);

	public synchronized <T extends GSContent> void register(Class<T> klass, String description, ContentProvider<T> provider) {
		try {
			Assert.isLegal(!byClass.containsKey(klass), "A content provider for " + klass + " is already registered");

			prefetchContentTracker.setValue(DownloadState.NOT_STARTED);

			ContentType<T> ctype = new ContentType<>(klass, description);
			types.add(ctype);
			DownloadManager downloader = downloadManagerFor(klass);
			byClass.put(klass, new TypedContentManager<>(downloader, provider));
		} catch (Throwable e) {
			BootWizardActivator.log(e);
		}
	}

	/**
	 * Factory method to create a DownloadManager for a given content type name
	 */
	public DownloadManager downloadManagerFor(Class<?> contentType) throws IllegalStateException, IOException {
		return new DownloadManager(new AuthenticatedDownloader(),
				new File(BootWizardActivator.getDefault().getStateLocation().toFile(), contentType.getSimpleName()))
						.clearCache();
	}



	/**
	 * Fetch the content of a given type. May return null but only if no content
	 * provider has been registered for the type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] get(Class<T> type) {

		TypedContentManager<T> man = (TypedContentManager<T>) byClass.get(type);
		if (man != null) {
			return man.getAll();
		}
		return null;
	}

	public synchronized ContentType<?>[] getTypes() {
		return types.toArray(new ContentType<?>[types.size()]);
	}

	public Object[] get(ContentType<?> ct) {
		if (ct != null) {
			return get(ct.getKlass());
		}
		return null;
	}

	/**
	 * Will return content for the given content type, IF and only if, there is no prefetching currently under way. Otherwise it returns null.
	 * @param ct
	 * @return content if no downloading is currently taking place. Null otherwise
	 */
	public Object[] getWithPrefetchCheck(ContentType<?> ct) {
		if (prefetchContentTracker.getValue() != DownloadState.IS_DOWNLOADING) {
			return get(ct);
		}
		return null;
	}

	/**
	 * Creates a content manager that contains just a single content item.
	 */
	public static <T extends GSContent> ContentManager singleton(final Class<T> type, final String description, final T item) {
		ContentManager cm = new ContentManager();
		cm.register(type, description, new ContentProvider<T>() {
			// @Override
			@Override
			public T[] fetch(DownloadManager downloader) {
				@SuppressWarnings("unchecked")
				T[] array = (T[]) Array.newInstance(type, 1);
				item.setDownloader(downloader);
				array[0] = item;
				return array;
			}
		});
		return cm;
	}

	/**
	 * Prefetch all the content. May require network access therefore do not run in the UI thread.
	 */
	public void prefetchAllContent(IProgressMonitor monitor) {

		prefetchContentTracker.setValue(DownloadState.IS_DOWNLOADING);
		prefetchContentError = null;
		try {
			ContentType<?>[] allTypes = getTypes();

			if (allTypes != null) {
				for (ContentType<?> type : allTypes) {
					if (monitor.isCanceled()) {
						break;
					}
					get(type);
				}
			}
			// Inform any listeners that completion has just finished (e.g. to let listeners refresh the content in their views)
			prefetchContentTracker.setValue(DownloadState.DOWNLOADING_COMPLETED);
		} catch (Throwable e) {
			prefetchContentError = e;
			prefetchContentTracker.setValue(DownloadState.DOWNLOADING_FAILED);
			throw e;
		} finally {
			// Mark the session as downloaded
			prefetchContentTracker.setValue(DownloadState.DOWNLOADED);
		}
	}

	public LiveVariable<DownloadState> getPrefetchContentTracker() {
		return prefetchContentTracker;
	}

	public Throwable getPrefetchContentError() {
		return prefetchContentError;
	}

	public LiveVariable<DownloadState> getPrefetchContentProviderPropertiesTracker() {
		return prefetchContentProviderPropertiesTracker;
	}

	/**
	 * Will run {@link #prefetch(IProgressMonitor)} in the background.
	 * @param runnableContext. Must not be null.
	 */
	public void prefetchInBackground(IRunnableContext runnableContext) {
		try {
			JobUtil.runBackgroundJobWithUIProgress((monitor) -> {

				prefetch(monitor);

			}, runnableContext, "Prefetching content...");
		} catch (Exception e) {
			BootWizardActivator.log(e);
		}
	}

	/**
	 * Will prefetch (download) all content. This may be a long-running process
	 * and is not expected to be run in the UI thread.
	 *
	 * @param monitor
	 *            must not be null
	 */
	protected void prefetch(IProgressMonitor monitor) {
		String downloadLabel = "Downloading all content. Please wait...";
		prefetchAllContent(SubMonitor.convert(monitor, downloadLabel, 50));
	}

	/**
	 * Disposes all prefetching tracking listeners. However, tracking states are preserved.
	 */
	public void disposePrefetchTrackingListeners() {
		// The purpose of disposing trackers is to clear any listeners that are registered to avoid possible memory leaks
		// on accumulating listeners over time, but the tracker states should be preserved.
		DownloadState propertiesDownloadState = prefetchContentProviderPropertiesTracker.getValue();
		DownloadState contentDownloadState = prefetchContentTracker.getValue();

		// Dispose all listeners to avoid memory leaks for listeners that keep getting accumulating over time.
		prefetchContentProviderPropertiesTracker.dispose();
		prefetchContentTracker.dispose();

		// Make sure new trackers are set
		prefetchContentProviderPropertiesTracker = new LiveVariable<>(propertiesDownloadState);
		prefetchContentTracker = new LiveVariable<>(contentDownloadState);
	}

	public static enum DownloadState {
		/**
		 * Downloading currently under way
		 */
		IS_DOWNLOADING,

		/**
		 * Active downloading session has just completed succesfully. For example, this state may be used to trigger
		 * refresh of content in views.
		 */
		DOWNLOADING_COMPLETED,

		/**
		 * Active downloading sessions has just completed with an exception. An explanation of the error may be retrieved
		 * by calling the 'getException' method
		 */
		DOWNLOADING_FAILED,

		/**
		 * A "Post-downloading" state, different than
		 * {@link #DOWNLOADING_COMPLETED}, that indicates that all content has
		 * already been downloaded before, and potentially no further action is
		 * required on any participants that are listening for this state.
		 */
		DOWNLOADED,

		/**
		 * No downloading has been initiated yet
		 */
		NOT_STARTED
	}
}
