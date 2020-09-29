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
package org.springsource.ide.eclipse.commons.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.springsource.ide.eclipse.commons.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;
import org.springsource.ide.eclipse.commons.internal.core.net.HttpClientTransportService;
import org.springsource.ide.eclipse.commons.internal.core.net.ITransportService;
import org.springsource.ide.eclipse.commons.internal.core.net.P2TransportService;

/**
 * Provides helper methods for downloading files.
 * @author Steffen Pingel
 */
public class HttpUtil {

	private static ITransportService transport;

	public static IStatus download(String url, File archiveFile, File targetDirectory, IProgressMonitor monitor) {
		return download(url, archiveFile, targetDirectory, null, monitor);
	}

	public static IStatus download(String url, File archiveFile, File targetDirectory, String prefix,
			IProgressMonitor monitor) {

		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		SubMonitor progress = SubMonitor.convert(monitor, 100);

		targetDirectory.mkdirs();

		// download archive file
		try {
			try {
				OutputStream out = new BufferedOutputStream(new FileOutputStream(archiveFile));
				try {
					HttpUtil.download(new URI(url), out, progress.newChild(70));
				}
				catch (CoreException e) {
					return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, NLS.bind(
							"I/O error while retrieving data: {0}", e.getMessage()), e);
				}
				catch (URISyntaxException e) {
					return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, NLS.bind("Invalid URL: {0}", url), e);
				}
				finally {
					out.close();
				}
			}
			catch (IOException e) {
				return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "I/O error while retrieving data", e);
			}

			// extract archive file
			try {
				URL fileUrl = archiveFile.toURI().toURL();
				ZipFileUtil.unzip(fileUrl, targetDirectory, prefix, progress.newChild(30));
				if (targetDirectory.listFiles().length <= 0) {
					String message = NLS.bind("Zip file {0} appears to be empty", archiveFile);
					return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, message);
				}
			}
			catch (IOException e) {
				return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Error while extracting archive", e);
			}
		}
		finally {
			archiveFile.delete();
		}
		return Status.OK_STATUS;
	}

	public static void download(URI uri, OutputStream out, IProgressMonitor monitor) throws CoreException {
		String protocol = uri.getScheme();
		if ("file".equals(protocol)) {
			// Yes. it is a bit strange that HttpUtil knows how to read from
			// file url. But it is just easier that
			// way. Don't need to special case file urls in other places.
			// We should consider renaming this class but it has the potential
			// of breaking a lot of dependencies.
			File f = new File(uri);
			FileInputStream contents = null;
			try {
				contents = new FileInputStream(f);
				byte[] buf = new byte[40 * 1024];
				int read;
				while ((read = contents.read(buf)) >= 0) {
					// read = -1 means EOF
					// read == 0 probably is impossible but handle it anyway.
					if (read > 0) {
						out.write(buf, 0, read);
					}
				}
			}
			catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, e.getMessage(), e));
			}
			finally {
				try {
					if (contents != null) {
						contents.close();
					}
				}
				catch (IOException e) {
				}
			}
		}
		else {
			getTransport().download(uri, out, monitor);
		}
	}

	public static long getLastModified(URI location, IProgressMonitor monitor) throws CoreException {
		return getTransport().getLastModified(location, monitor);
	}

	public static synchronized ITransportService getTransport() {
		if (transport == null) {
			if (Platform.isRunning()) {
				try {
					transport = new P2TransportService();
				}
				catch (ClassNotFoundException e) {
					// fall back to HttpClientTransport
				}
			}
			if (transport == null) {
				transport = new HttpClientTransportService();
			}
		}
		return transport;
	}

	public static InputStream stream(URI uri, IProgressMonitor monitor) throws CoreException {
		return getTransport().stream(uri, monitor);
	}

	public static void ping(URI uri) throws MalformedURLException, IOException, CoreException {
		URLConnection connection = uri.toURL().openConnection();
		connection.setConnectTimeout(500);
		InputStream input = connection.getInputStream();
		IOUtil.consume(input);
	}

}
