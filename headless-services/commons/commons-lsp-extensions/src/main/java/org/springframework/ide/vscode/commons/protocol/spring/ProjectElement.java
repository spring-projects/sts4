/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.spring;

import java.util.Iterator;
import java.util.List;

public class ProjectElement extends AbstractSpringIndexElement {

	private String projectName;

	public ProjectElement(String projectName) {
		this.projectName = projectName;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void removeDocument(String docURI) {
		List<SpringIndexElement> children = this.getChildren();
		
		for (Iterator<SpringIndexElement> iterator = children.iterator(); iterator.hasNext();) {
			SpringIndexElement springIndexElement = (SpringIndexElement) iterator.next();
			if (springIndexElement instanceof DocumentElement doc && doc.getDocURI().equals(docURI)) {
				iterator.remove();
			}
		}
	}

}
