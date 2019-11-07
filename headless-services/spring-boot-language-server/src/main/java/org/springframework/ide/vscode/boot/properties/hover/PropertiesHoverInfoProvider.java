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

package org.springframework.ide.vscode.boot.properties.hover;

import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtilProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.IRegion;

import reactor.util.function.Tuple2;

public class PropertiesHoverInfoProvider implements HoverInfoProvider {

	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;
	private JavaProjectFinder projectFinder;
	private SourceLinks sourceLinks;

	public PropertiesHoverInfoProvider(SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider, JavaProjectFinder projectFinder, SourceLinks sourceLinks) {
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
		this.projectFinder = projectFinder;
		this.sourceLinks = sourceLinks;
	}

	@Override
	public Tuple2<Renderable, IRegion> getHoverInfo(IDocument document, int offset) throws Exception {
		return new PropertiesHoverCalculator(indexProvider.getIndex(document).getProperties(),
					typeUtilProvider.getTypeUtil(sourceLinks, document), document, offset).calculate();
	}
}
