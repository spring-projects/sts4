/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.maintype;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

public interface IMainTypeFinder {

	/**
	 * Finds main types in a given Java project. If no types are found, an empty
	 * value is returned.
	 * 
	 * @param project
	 *            java project where main types should be searched.
	 * @param mon
	 * @return non-null list of main types in the project. Return empty value if
	 *         no main types are found.
	 * @throws Exception
	 *             if failure occurred while finding main types
	 */
	public IType[] findMain(IJavaProject project, IProgressMonitor mon)
			throws Exception;

}
