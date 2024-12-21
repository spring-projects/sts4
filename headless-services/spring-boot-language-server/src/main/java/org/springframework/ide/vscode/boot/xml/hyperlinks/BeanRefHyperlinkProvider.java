/*******************************************************************************
 * Copyright (c) 2019, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.hyperlinks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Alex Boyko
 */
public class BeanRefHyperlinkProvider implements XMLHyperlinkProvider {
	
	private final JavaProjectFinder projectFinder;
	private final SpringMetamodelIndex springIndex;

	public BeanRefHyperlinkProvider(JavaProjectFinder projectFinder, SpringMetamodelIndex springIndex) {
		this.projectFinder = projectFinder;
		this.springIndex = springIndex;
	}

	@Override
	public List<Location> getDefinition(TextDocument doc, String namespace, DOMNode node, DOMAttr attributeAt) {
		Optional<IJavaProject> foundProject = this.projectFinder.find(doc.getId());
		if (foundProject.isPresent()) {
			final IJavaProject project = foundProject.get();
			
			String beanID = attributeAt.getValue();
			Bean[] beans = springIndex.getBeansWithName(project.getElementName(), beanID);
			
			if (beans != null && beans.length > 0) {
				return Arrays.stream(beans)
					.map(bean -> bean.getLocation())
					.toList();
			}
		}
		return null;
	}
	
}
