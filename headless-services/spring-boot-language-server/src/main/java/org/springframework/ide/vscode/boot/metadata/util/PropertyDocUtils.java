/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata.util;

import java.util.Optional;

import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.commons.java.IJavaElement;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMember;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Boot properties documentation info utils
 * 
 * @author Alex Boyko
 *
 */
public class PropertyDocUtils {
	
	/**
	 * Generates documentation for boot property coming from java element
	 * 
	 * @param sourceLinks
	 * @param project
	 * @param je
	 * @return
	 */
	public static Renderable documentation(SourceLinks sourceLinks, IJavaProject project, IJavaElement je) {
		IJavadoc javadoc = je.getJavaDoc();
		Builder<Renderable> renderableBuilder = ImmutableList.builder();
		renderableBuilder.add(javadoc == null ? Renderables.NO_DESCRIPTION: javadoc.getRenderable());
		if (je instanceof IMember) {
			IType containingType = je instanceof IType ? (IType) je : ((IMember)je).getDeclaringType();						
			if (je != null) {
				renderableBuilder.add(Renderables.lineBreak());
				renderableBuilder.add(Renderables.text("Type: "));
				String type = containingType.getFullyQualifiedName();
				Optional<String> url = SourceLinkFactory.createSourceLinks(null).sourceLinkUrlForFQName(project, type);
				if (url.isPresent()) {
					renderableBuilder.add(Renderables.link(type, url.get()));
				} else {
					renderableBuilder.add(Renderables.inlineSnippet(Renderables.text(type)));
				}
			}
		}
		return Renderables.concat(renderableBuilder.build());
	}

}
