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

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * @author Steffen Pingel
 */
public class P2TransportService implements ITransportService {

	private Object transport;

	private Method downloadMethod;

	private Method streamMethod;

	private Method getLastModifiedMethod;

	public P2TransportService() throws ClassNotFoundException {
		// TODO e3.5 remove reflection
		try {
			Class<?> clazz;
			try {
				clazz = Class.forName("org.eclipse.equinox.internal.p2.repository.RepositoryTransport"); //$NON-NLS-1$
				Method getInstanceMethod = clazz.getDeclaredMethod("getInstance"); //$NON-NLS-1$
				transport = getInstanceMethod.invoke(null);
			}
			catch (ClassNotFoundException e) {
				// the class moved to a different bundle in 3.7
				transport = getTransport_e3_7();
				clazz = transport.getClass();
			}
			downloadMethod = clazz.getDeclaredMethod("download", URI.class, OutputStream.class, IProgressMonitor.class); //$NON-NLS-1$
			streamMethod = clazz.getDeclaredMethod("stream", URI.class, IProgressMonitor.class); //$NON-NLS-1$
			getLastModifiedMethod = clazz.getDeclaredMethod("getLastModified", URI.class, IProgressMonitor.class); //$NON-NLS-1$
		}
		catch (LinkageError e) {
			throw new ClassNotFoundException("Failed to load P2 transport", e); //$NON-NLS-1$
		}
		catch (Exception e) {
			throw new ClassNotFoundException("Failed to load P2 transport", e); //$NON-NLS-1$
		}
	}

	private static Object getTransport_e3_7() throws Exception {
		// This line is here merely to make sure that the bundle gets activated
		// before trying to use the service (to get rid of a race condition).
		Class<?> clazz = Class.forName("org.eclipse.equinox.p2.core.IProvisioningAgent"); //$NON-NLS-1$

		BundleContext bundleContext = CorePlugin.getDefault().getBundle().getBundleContext();
		ServiceReference serviceReference = bundleContext
				.getServiceReference("org.eclipse.equinox.p2.core.IProvisioningAgent");
		if (serviceReference != null) {
			try {
				Object agent = bundleContext.getService(serviceReference);
				if (agent != null) {
					Method getServiceMethod = agent.getClass().getDeclaredMethod("getService", String.class); //$NON-NLS-1$
					return getServiceMethod.invoke(agent, "org.eclipse.equinox.internal.p2.repository.Transport");
				}
			}
			finally {
				bundleContext.ungetService(serviceReference);
			}
		}
		throw new RuntimeException("Transport service not available");
	}

	private void convertException(InvocationTargetException e) throws CoreException {
		if (e.getCause() instanceof CoreException) {
			throw (CoreException) e.getCause();
		}
		else {
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, e.getCause().getMessage(),
					e.getCause()));
		}
	}

	public void download(URI uri, OutputStream out, IProgressMonitor monitor) throws CoreException {
		try {
			IStatus result = (IStatus) downloadMethod.invoke(transport, uri, out, monitor);
			if (result.getSeverity() == IStatus.CANCEL) {
				throw new OperationCanceledException();
			}
			if (!result.isOK()) {
				throw new CoreException(result);
			}
		}
		catch (InvocationTargetException e) {
			if (e.getCause() instanceof CoreException) {
				throw (CoreException) e.getCause();
			}
		}
		catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public long getLastModified(URI location, IProgressMonitor monitor) throws CoreException {
		try {
			return (Long) getLastModifiedMethod.invoke(transport, location, monitor);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			convertException(e);
		}
		// should never happen
		throw new IllegalStateException();
	}

	public InputStream stream(URI uri, IProgressMonitor monitor) throws CoreException {
		try {
			return (InputStream) streamMethod.invoke(transport, uri, monitor);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e) {
			convertException(e);
		}
		// should never happen
		throw new IllegalStateException();
	}

}
