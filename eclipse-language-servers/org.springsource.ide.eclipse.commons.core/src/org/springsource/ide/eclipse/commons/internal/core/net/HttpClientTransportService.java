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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.osgi.util.NLS;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * A utility for accessing web resources.
 * @author Steffen Pingel
 */
public class HttpClientTransportService implements ITransportService {

	private static final int BUFFER_SIZE = 4 * 1024;

	public HttpClientTransportService() {
	}

	/**
	 * Download an HTTP-based resource
	 * 
	 * @param target the target file to which the content is saved
	 * @param location the web location of the content
	 * @param monitor the monitor
	 * @throws IOException if a network or IO problem occurs
	 */
	public void download(java.net.URI uri, OutputStream out, IProgressMonitor progressMonitor) throws CoreException {
		WebLocation location = new WebLocation(uri.toString());
		SubMonitor monitor = SubMonitor.convert(progressMonitor);
		monitor.subTask(NLS.bind("Fetching {0}", location.getUrl()));
		try {
			HttpClient client = new HttpClient();
			org.eclipse.mylyn.commons.net.WebUtil.configureHttpClient(client, ""); //$NON-NLS-1$

			GetMethod method = new GetMethod(location.getUrl());
			try {
				HostConfiguration hostConfiguration = org.eclipse.mylyn.commons.net.WebUtil.createHostConfiguration(
						client, location, monitor);
				int result = org.eclipse.mylyn.commons.net.WebUtil.execute(client, hostConfiguration, method, monitor);
				if (result == HttpStatus.SC_OK) {
					long total = method.getResponseContentLength();
					if (total != -1) {
						monitor.setWorkRemaining((int) total);
					}
					InputStream in = org.eclipse.mylyn.commons.net.WebUtil.getResponseBodyAsStream(method, monitor);
					try {
						in = new BufferedInputStream(in);
						byte[] buffer = new byte[BUFFER_SIZE];
						int len;
						while ((len = in.read(buffer)) != -1) {
							out.write(buffer, 0, len);
							if (total != -1) {
								monitor.worked(len);
							}
							else {
								monitor.worked(1);
								monitor.setWorkRemaining(10000);
							}

							if (monitor.isCanceled()) {
								// this point is reached if the user requests a
								// cancellation
								throw new OperationCanceledException();
							}

						}
					}
					catch (OperationCanceledException e) {
						// this point is reached if there is some problem with
						// the download
						throw toException(location, result);
					}
					catch (IOException e) {
						// this point is reached if there is some problem with
						// the network
						throw toException(location, 500);
					}
					finally {
						in.close();
					}
				}
				else {
					throw toException(location, result);
				}
			}
			finally {
				method.releaseConnection();
			}
		}
		catch (IOException e) {
			throw toException(location, e);
		}
		finally {
			monitor.done();
		}
	}

	/**
	 * Verify availability of resources at the given web locations. Normally
	 * this would be done using an HTTP HEAD.
	 * 
	 * @param locations the locations of the resource to verify
	 * @param one indicate if only one of the resources must exist
	 * @param progressMonitor the monitor
	 * @return true if the resource exists
	 */
	public long getLastModified(java.net.URI uri, IProgressMonitor progressMonitor) throws CoreException {
		WebLocation location = new WebLocation(uri.toString());
		SubMonitor monitor = SubMonitor.convert(progressMonitor);
		monitor.subTask(NLS.bind("Fetching {0}", location.getUrl()));
		try {
			HttpClient client = new HttpClient();
			org.eclipse.mylyn.commons.net.WebUtil.configureHttpClient(client, ""); //$NON-NLS-1$

			HeadMethod method = new HeadMethod(location.getUrl());
			try {
				HostConfiguration hostConfiguration = org.eclipse.mylyn.commons.net.WebUtil.createHostConfiguration(
						client, location, monitor);
				int result = org.eclipse.mylyn.commons.net.WebUtil.execute(client, hostConfiguration, method, monitor);
				if (result == HttpStatus.SC_OK) {
					Header lastModified = method.getResponseHeader("Last-Modified"); //$NON-NLS-1$
					if (lastModified != null) {
						try {
							return DateUtil.parseDate(lastModified.getValue()).getTime();
						}
						catch (DateParseException e) {
							// fall through
						}
					}
					return 0;
				}
				else {
					throw toException(location, result);
				}
			}
			catch (IOException e) {
				throw toException(location, e);
			}
			finally {
				method.releaseConnection();
			}
		}
		finally {
			monitor.done();
		}
	}

	/**
	 * Read a web-based resource at the specified location using the given
	 * processor.
	 * 
	 * @param location the web location of the content
	 * @param processor the processor that will handle content
	 * @param progressMonitor the monitor
	 * @throws IOException if a network or IO problem occurs
	 */
	public InputStream stream(java.net.URI uri, IProgressMonitor progressMonitor) throws CoreException {
		WebLocation location = new WebLocation(uri.toString());
		SubMonitor monitor = SubMonitor.convert(progressMonitor);
		monitor.subTask(NLS.bind("Fetching {0}", location.getUrl()));
		try {
			HttpClient client = new HttpClient();
			org.eclipse.mylyn.commons.net.WebUtil.configureHttpClient(client, ""); //$NON-NLS-1$

			boolean success = false;
			GetMethod method = new GetMethod(location.getUrl());
			try {
				HostConfiguration hostConfiguration = org.eclipse.mylyn.commons.net.WebUtil.createHostConfiguration(
						client, location, monitor);
				int result = org.eclipse.mylyn.commons.net.WebUtil.execute(client, hostConfiguration, method, monitor);
				if (result == HttpStatus.SC_OK) {
					InputStream in = org.eclipse.mylyn.commons.net.WebUtil.getResponseBodyAsStream(method, monitor);
					success = true;
					return in;
				}
				else {
					throw toException(location, result);
				}
			}
			catch (IOException e) {
				throw toException(location, e);
			}
			finally {
				if (!success) {
					method.releaseConnection();
				}
			}
		}
		finally {
			monitor.done();
		}
	}

	private CoreException toException(WebLocation location, int result) {
		return new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, NLS.bind(
				"Download of {0} failed: Unexpected HTTP response {1}", location.getUrl(), result)));
	}

	private CoreException toException(WebLocation location, IOException e) throws CoreException {
		String message = e.getMessage() != null ? e.getMessage() : "Unexpected error";
		return new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, NLS.bind(
				"Download of {0} failed: {1}", location.getUrl(), message), e));
	}

}
