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
import org.springframework.ide.vscode.boot.xml.completions.PropertyNameCompletionProposalProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Alex Boyko
 */
public class PropertyNameHyperlinkProvider implements XMLHyperlinkProvider {
	
	private final JavaProjectFinder projectFinder;
	private final JavaElementLocationProvider locationProvider;
	
	public PropertyNameHyperlinkProvider(JavaProjectFinder projectFinder, JavaElementLocationProvider locationProvider) {
		this.projectFinder = projectFinder;
		this.locationProvider = locationProvider;
	}

	@Override
	public Location getDefinition(TextDocument doc, String namespace, DOMNode node, DOMAttr attributeAt) {
		Optional<IJavaProject> foundProject = this.projectFinder.find(doc.getId());
		String propertyName = attributeAt.getValue();
		if (foundProject.isPresent() && propertyName != null && !propertyName.isEmpty()) {
			IJavaProject project = foundProject.get();
			String beanClass = PropertyNameCompletionProposalProvider.identifyBeanClass(node);
			if (beanClass != null && beanClass.length() > 0) {
				return PropertyNameCompletionProposalProvider.propertyNameCandidateMethods(project, beanClass)
					.filter(method -> propertyName.equals(PropertyNameCompletionProposalProvider.getPropertyName(method)))
					.map(method -> locationProvider.findLocation(project, method))
					.findFirst()
					.orElse(null);
			}
		}
		return null;
	}
	
}
