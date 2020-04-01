/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.hyperlinks;

import java.util.Optional;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.boot.java.links.JavaElementLocationProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Alex Boyko
 */
public class JavaTypeHyperlinkProvider implements XMLHyperlinkProvider {

	private final JavaProjectFinder projectFinder;
	private final JavaElementLocationProvider locationProvider;

	public JavaTypeHyperlinkProvider(JavaProjectFinder projectFinder, JavaElementLocationProvider locationProvider) {
		this.projectFinder = projectFinder;
		this.locationProvider = locationProvider;
	}

	@Override
	public Location getDefinition(TextDocument doc, String namespace, DOMNode node, DOMAttr attributeAt) {
		Optional<IJavaProject> foundProject = this.projectFinder.find(doc.getId());
		if (foundProject.isPresent()) {
			IJavaProject project = foundProject.get();
			String fqName = attributeAt.getValue();
			if (fqName != null) {
				IType type = project.getIndex().findType(fqName);
				if (type != null) {
					return locationProvider.findLocation(project, type);
				}
			}
		}
		return null;
	}

}
