/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.links;

import java.nio.file.Path;
import java.util.Optional;

import org.openrewrite.java.tree.J.CompilationUnit;
import org.springframework.ide.vscode.boot.java.utils.ORCompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;

import reactor.util.function.Tuple2;

/**
 * VSCode specific source links implementation
 *
 * @author Alex Boyko
 *
 */
public class VSCodeSourceLinks extends AbstractSourceLinks {

	public VSCodeSourceLinks(ORCompilationUnitCache cuCache, JavaProjectFinder projectFinder) {
		super(cuCache, projectFinder);
	}

	@Override
	public Optional<String> sourceLinkForResourcePath(Path path) {
		return Optional.of(path.toUri().toString());
	}

	@Override
	protected String positionLink(CompilationUnit cu, String fqName) {
		if (cu != null) {
			Tuple2<Integer, Integer> region = findTypeRegion(cu, fqName);
			if (region != null) {
				int column = region.getT2();
				int line = region.getT1();
				StringBuilder sb = new StringBuilder();
				sb.append('#');
				sb.append(line);
				sb.append(',');
				sb.append(column);
				return sb.toString();
			}
		}
		return null;
	}

	@Override
	protected Optional<String> jarLinkUrl(IJavaProject project, String fqName, IJavaModuleData jarModuleData) {
		return Optional.ofNullable(JdtJavaDocumentUriProvider.uri(project, fqName)).map(uri -> uri.toString());
	}

}
