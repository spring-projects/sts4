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
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.Region;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * VSCode specific source links implementation
 * 
 * @author Alex Boyko
 *
 */
public class VSCodeSourceLinks extends AbstractSourceLinks {
	
	private static Supplier<Logger> LOG = Suppliers.memoize(() -> LoggerFactory.getLogger(AbstractSourceLinks.class));

	public VSCodeSourceLinks(BootJavaLanguageServer server) {
		super(server);
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
	protected Optional<String> jarUrl(IJavaProject project, String fqName, File jarFile) {
		try {
			int lastDotIndex = fqName.lastIndexOf('.');
			String packageName = fqName.substring(0, lastDotIndex);
			String typeName = fqName.substring(lastDotIndex + 1);
			String jarFileName = jarFile.getName();
			StringBuilder sb = new StringBuilder();
			sb.append("jdt://contents/");
			sb.append(jarFileName);
			sb.append("/");
			sb.append(packageName);
			sb.append("/");
			sb.append(typeName);
			sb.append(CLASS);
			sb.append("?");

			StringBuilder query = new StringBuilder();
			query.append("=");
			query.append(project.getElementName());
			query.append("/");
			String convertedPath = jarFile.toString().replace(File.separator, "\\/");
			query.append(convertedPath);
			query.append("<");
			query.append(packageName);
			query.append("(");
			query.append(typeName);
			query.append(CLASS);

			sb.append(URLEncoder.encode(query.toString(), "UTF8"));
			
			return Optional.of(sb.toString());
		} catch (Throwable t) {
			LOG.get().warn("Failed creating source URI for jar " + jarFile + " type " + fqName + " in the context of project " + project.getElementName(), t);
		}
		return Optional.empty();
	}

}
