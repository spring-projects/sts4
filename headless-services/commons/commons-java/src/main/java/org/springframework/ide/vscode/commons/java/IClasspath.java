/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.protocol.java.Classpath.CPE;

/**
 * Classpath for a Java artifact
 *
 * @author Kris De Volder
 * @author Alex Boyko
 *
 */
public interface IClasspath {

	public static final Logger log = LoggerFactory.getLogger(IClasspath.class);

	String getName();

	/**
	 * Classpath entries paths
	 *
	 * @return collection of classpath entries in a form file/folder paths
	 * @throws Exception
	 */
	Collection<CPE> getClasspathEntries() throws Exception;
	}
