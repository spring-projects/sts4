/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces;

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionResolver;

public interface INamespaceManager {

	void unregisterNamespaceDefinitionListener(INamespaceDefinitionListener listener);
	void registerNamespaceDefinitionListener(INamespaceDefinitionListener listener);
	void notifyNamespaceDefinitionListeners(IProject project);
	
	INamespaceDefinitionResolver getNamespaceDefinitionResolver();
	CompletableFuture<?> nameSpaceHandlersReady();
}
