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
import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Steffen Pingel
 */
public interface ITransportService {

	public abstract void download(URI uri, OutputStream out, IProgressMonitor monitor) throws CoreException;

	public abstract long getLastModified(URI location, IProgressMonitor monitor) throws CoreException;

	public abstract InputStream stream(URI uri, IProgressMonitor monitor) throws CoreException;

}
