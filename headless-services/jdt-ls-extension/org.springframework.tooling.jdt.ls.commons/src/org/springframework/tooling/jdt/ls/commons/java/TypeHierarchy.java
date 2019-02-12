/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.resources.ResourceUtils;

public class TypeHierarchy {
	
	private Logger logger;
	private JavaData javaData;
	
	public TypeHierarchy(Logger logger, JavaData javaData) {
		super();
		this.logger = logger;
		this.javaData = javaData;
	}
	
	private static String jdtCompatibleFqName(String fqName) {
		return fqName.replace('$', '.');
	}
	
	private ITypeHierarchy hierarchy(URI projectUri, String fqName, boolean superTypes) {
		try {
			if (projectUri == null) {
				for (IJavaProject jp : ResourceUtils.allJavaProjects()) {
					IType type = jp.findType(jdtCompatibleFqName(fqName));
					if (type != null) {
						return superTypes ? type.newSupertypeHierarchy(new NullProgressMonitor()) : type.newTypeHierarchy(new NullProgressMonitor());
					}
				}
				
			} else {
				IJavaProject javaProject = projectUri == null ? null : ResourceUtils.getJavaProject(projectUri);
				if (javaProject != null) {
					IType type = javaProject.findType(jdtCompatibleFqName(fqName));
					if (type != null) {
						return superTypes ? type.newSupertypeHierarchy(new NullProgressMonitor()) : type.newTypeHierarchy(javaProject, new NullProgressMonitor());
					}
				}
			}
		} catch (Exception e) {
			logger.log(e);
		}
		return null;
	}
	
	public Stream<TypeData> subTypes(URI projectUri, String fqName) {
		ITypeHierarchy hierarchy = hierarchy(projectUri, fqName, false);
		if (hierarchy != null) {
			return Arrays.stream(hierarchy.getAllSubtypes(hierarchy.getType())).parallel().map(javaData::createTypeData);
		}
		return Stream.of();
	}

	public Stream<TypeData> superTypes(URI projectUri, String fqName) {
		ITypeHierarchy hierarchy = hierarchy(projectUri, fqName, true);
		if (hierarchy != null) {
			return Arrays.stream(hierarchy.getAllSupertypes(hierarchy.getType()))
				.parallel()
				.map(javaData::createTypeData);
		}
		return Stream.of();
	}
}
