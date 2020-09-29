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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.contentassist;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

/**
 * @author Nieraj Singh
 */
public interface IProjectContentAssistProcessor extends IContentAssistProcessor {
	
	/**
	 * 
	 * @return non-null project context used for content assist proposal
	 */
	public IJavaProject getProject();
	
	/**
	 * This MUST be set before completion can obtain proposals. Must not be null
	 * @param non null java project set PRIOR to invoking content assist.
	 */
	public void setProject(IJavaProject project);
}
