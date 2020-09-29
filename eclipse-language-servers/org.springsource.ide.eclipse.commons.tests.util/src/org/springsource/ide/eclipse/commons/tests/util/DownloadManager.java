/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.tests.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.springsource.ide.eclipse.commons.core.HttpUtil;


/**
 * Manages a cache of downloaded files used by tests.
 *
 * @author Kris De Volder, Steffen Pingel
 *
 * @since 2.8
 */
public class DownloadManager {

	/**
	 * An instance of this interface represent an action to execute on a downloaded
	 * File. The action may indicate failure by throwing an exception or by
	 * returning false. A failed action may trigger the DownloadManager to
	 * clear the cache and try again for a limited number of times.
	 */
	public interface DownloadRequestor {
		void exec(File downloadedFile) throws Exception;
	}

	private final String cacheDirectory;

	private static DownloadManager defaultInstance = null;

	public static DownloadManager getDefault() {
		if (defaultInstance==null) {
			defaultInstance = new DownloadManager();
		}
		return defaultInstance;
	}

	public DownloadManager() {
		this(System.getProperty(
				"com.springsource.sts.tests.cache",
				System.getProperty("user.home") + File.separatorChar + ".sts-test-cache"));
		deleteBuildSnapshots();
	}

	/**
	 * Build snapshots from a previous test run shouldn't be used from the cache. So delete them
	 * when the DownloadManager instance is created.
	 */
	private void deleteBuildSnapshots() {
		//Only do this on the build site, locally it is easy enough to delete buildsnaps manually
		// as needed/desired.
		if (StsTestUtil.isOnBuildSite()) {
			File cache = new File(cacheDirectory);
			if (cache.isDirectory()) {
				String[] names = cache.list();
				for (String name : names) {
					if (name.contains("SNAPSHOT")) {
						try {
							new File(cache, name).delete();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public DownloadManager(String cacheDir) {
		this.cacheDirectory = cacheDir;
	}

	/**
	 * This method is deprecated, please use doWithDownload to provide proper recovery
	 * for cache corruption.
	 */
	@Deprecated
	public File downloadFile(URI uri) throws URISyntaxException, FileNotFoundException, CoreException, IOException {
		String protocol = uri.getScheme();
		if ("file".equals(protocol)) {
			return new File(uri);
		}

		String path = uri.getPath();
		int i = path.lastIndexOf("/");
		if (i >= 0) {
			path = path.substring(i + 1);
		}

		File target = new File(cacheDirectory, path);
		if (target.exists()) {
			return target;
		}

		File cache = new File(cacheDirectory);
		if (!cache.exists()) {
			cache.mkdirs();
		}

		File targetPart = new File(cache, path + ".part");
		FileOutputStream out = new FileOutputStream(targetPart);
		try {
			System.out.println("Downloading " + uri + " to " + target);
			HttpUtil.download(uri, out, null);
		}
		finally {
			out.close();
		}

		if (!targetPart.renameTo(target)) {
			throw new IOException("Error while renaming " + targetPart + " to " + target);
		}

		return target;
	}

	/**
	 * This method tries to download or fetch a File from the cache, then passes the
	 * downloaded file to the DownloadRequestor.
	 * <p>
	 * If the requestor fails to properly execute on the downloaded file, the cache
	 * will be presumed to be corrupt. The file will be deleted from the cache
	 * and the download will be tried again. (for a limited number of times)
	 */
	public void doWithDownload(URI target, DownloadRequestor action) throws Exception {
		int tries = 4; // try at most X times
		Exception e = null;
		File downloadedFile = null;
		do {
			tries--;
			try {
				downloadedFile = downloadFile(target);
				action.exec(downloadedFile);
				return; // action and download succeeded without exceptions
			} catch (Exception caught) {
				caught.printStackTrace();
				//Presume the cache may be corrupt!
				System.out.println("Delete corrupt download: "+downloadedFile);
				//downloaded file may be null if download failed, rather than its processing:
				if (downloadedFile!=null) {
					downloadedFile.delete();
				}
				e = caught;
			}
		} while (tries>0);
		//Can only get here if action or download failed...
		//thus, e can not be null.
		throw e;
	}

	public File getCacheDir() {
		return new File(cacheDirectory);
	}

}
