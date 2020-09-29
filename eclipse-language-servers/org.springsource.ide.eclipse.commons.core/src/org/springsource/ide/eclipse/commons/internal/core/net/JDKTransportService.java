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
package org.springsource.ide.eclipse.commons.internal.core.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;


/**
 * @author Terry Denney
 */
public class JDKTransportService implements ITransportService {

	private static final int BUFFER_SIZE = 4 * 1024;

	public void download(URI uri, OutputStream out, IProgressMonitor progressMonitor) throws CoreException {
		SubMonitor monitor = SubMonitor.convert(progressMonitor);
		try {
			URL url = uri.toURL();

			monitor.subTask(NLS.bind("Fetching {0}", url));

			try {
				InputStream in = url.openStream();
				InputStream bufferedIn = new BufferedInputStream(in);
				try {
					byte[] buffer = new byte[BUFFER_SIZE];
					int len;
					while ((len = bufferedIn.read(buffer)) != -1) {
						out.write(buffer, 0, len);

						monitor.worked(1);
						monitor.setWorkRemaining(10000);

						if (monitor.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
				}
				finally {
					bufferedIn.close();
					in.close();
				}
			}
			catch (IOException e) {
				throw toException(url, e);
			}
		}
		catch (MalformedURLException e) {
			throw toException(uri, e);
		}
	}

	public long getLastModified(URI location, IProgressMonitor monitor) throws CoreException {
		try {
			URL url = location.toURL();
			try {
				return url.openConnection().getLastModified();
			}
			catch (IOException e) {
				throw toException(url, e);
			}
		}
		catch (MalformedURLException e) {
			throw toException(location, e);
		}
	}

	public InputStream stream(URI uri, IProgressMonitor monitor) throws CoreException {
		try {
			URL url = uri.toURL();

			try {
				InputStream in = url.openStream();
				return in;
			}
			catch (IOException e) {
				throw toException(url, e);
			}
		}
		catch (MalformedURLException e) {
			throw toException(uri, e);
		}
	}

	private CoreException toException(URI uri, IOException e) throws CoreException {
		String message = e.getMessage() != null ? e.getMessage() : "Unexpected error";
		return new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, NLS.bind(
				"Download of {0} failed: {1}", uri.getFragment(), message), e));
	}

	private CoreException toException(URL url, IOException e) throws CoreException {
		String message = e.getMessage() != null ? e.getMessage() : "Unexpected error";
		return new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, NLS.bind(
				"Download of {0} failed: {1}", url, message), e));
	}

}
