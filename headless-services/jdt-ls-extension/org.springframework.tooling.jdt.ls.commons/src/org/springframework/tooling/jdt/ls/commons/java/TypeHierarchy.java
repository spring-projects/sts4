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
package org.springframework.tooling.jdt.ls.commons.java;

import java.net.URI;
import java.util.stream.Stream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.commons.protocol.java.JavaTypeHierarchyParams;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.ide.vscode.commons.protocol.java.TypeDescriptorData;
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
	
	private ITypeHierarchy hierarchy(URI projectUri, String fqName, boolean superTypes) {
		try {
			String bindingKey = JavaData.toBindingKey(fqName);
			if (projectUri == null) {
				for (IJavaProject jp : ResourceUtils.allJavaProjects()) {
					// In case it's an anonymous inner type try the following rather than just find type on the project
					IType type = (IType) JavaData.findElement(jp, bindingKey);
					if (type != null) {
						return superTypes ? type.newSupertypeHierarchy(new NullProgressMonitor()) : type.newTypeHierarchy(new NullProgressMonitor());
					}
				}
				
			} else {
				IJavaProject javaProject = projectUri == null ? null : ResourceUtils.getJavaProject(projectUri);
				if (javaProject != null) {
					// In case it's an anonymous inner type try the following rather than just find type on the project
					IType type = (IType) JavaData.findElement(javaProject, bindingKey);
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
	
	public Stream<Either<TypeDescriptorData, TypeData>> subTypes(JavaTypeHierarchyParams params) {
		URI projectUri = params.getProjectUri() == null ? null : URI.create(params.getProjectUri());
		ITypeHierarchy hierarchy = hierarchy(projectUri, params.getFqName(), false);
		if (hierarchy != null) {
			IType focusType = hierarchy.getType();
			return Stream.concat(params.isIncludeFocusType() ? Stream.of(focusType) : Stream.empty(), Stream.of(hierarchy.getAllSubtypes(focusType)))
					.parallel()
					.map(type -> params.isDetailed() ? Either.forRight(javaData.createTypeData(type)) : Either.forLeft(javaData.createTypeDescriptorData(type)));
		}
		return Stream.of();
	}

	public Stream<Either<TypeDescriptorData, TypeData>> superTypes(JavaTypeHierarchyParams params) {
		URI projectUri = params.getProjectUri() == null ? null : URI.create(params.getProjectUri());
		ITypeHierarchy hierarchy = hierarchy(projectUri, params.getFqName(), true);
		if (hierarchy != null) {
			IType focusType = hierarchy.getType();
			return Stream.concat(params.isIncludeFocusType() ? Stream.of(focusType) : Stream.empty(), Stream.of(hierarchy.getAllSupertypes(focusType)))
				.parallel()
				.map(type -> params.isDetailed() ? Either.forRight(javaData.createTypeData(type)) : Either.forLeft(javaData.createTypeDescriptorData(type)));
		}
		return Stream.of();
	}
}
