/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public interface TypeLookup {

	public String getApplicationName();
	
	public IProject getProject();
	
	IType findType(String fqName);
	
	IProject[] relatedProjects();

}
