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
package org.springframework.ide.vscode.boot.java.links;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.Region;

/**
 * VSCode specific source links implementation
 *
 * @author Alex Boyko
 *
 */
public class VSCodeSourceLinks extends AbstractSourceLinks {

	public VSCodeSourceLinks(CompilationUnitCache cuCache) {
		super(cuCache);
	}

	@Override
	public Optional<String> sourceLinkForResourcePath(Path path) {
		return Optional.of(path.toUri().toString());
	}

	@Override
	protected String positionLink(CompilationUnit cu, String fqName) {
		if (cu != null) {
			Region region = findTypeRegion(cu, fqName);
			if (region != null) {
				int column = cu.getColumnNumber(region.getOffset());
				int line = cu.getLineNumber(region.getOffset());
				StringBuilder sb = new StringBuilder();
				sb.append('#');
				sb.append(line);
				sb.append(',');
				sb.append(column + 1); // 1-based columns?
				return sb.toString();
			}
		}
		return null;
	}

	@Override
	protected Optional<String> jarLinkUrl(IJavaProject project, String fqName, File jarFile) {
		return Optional.ofNullable(JdtJavaDocumentUriProvider.uri(project, fqName)).map(uri -> uri.toString());
	}

}
