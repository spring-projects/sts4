/*******************************************************************************
 * Copyright (c) 2009, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.model;

import org.eclipse.core.resources.IProject;

/**
 * Implementations of this interface will receive notifications when {@link INamespaceDefinition} are registered and
 * unregistered.
 * @author Christian Dupuis
 * @since 2.3.0
 */
public interface INamespaceDefinitionListener {

	/**
	 * Event notifying about a processed registration of a {@link INamespaceDefinition}.
	 */
	void onNamespaceDefinitionRegistered(NamespaceDefinitionChangeEvent event);

	/**
	 * Event notifying about a processed un-registration of a {@link INamespaceDefinition}.
	 */
	void onNamespaceDefinitionUnregistered(NamespaceDefinitionChangeEvent event);

	class NamespaceDefinitionChangeEvent {
		
		private final INamespaceDefinition namespaceDefinition;

		private final IProject project;
		
		public NamespaceDefinitionChangeEvent(INamespaceDefinition namespaceDefinition, IProject project) {
			this.namespaceDefinition = namespaceDefinition;
			this.project = project;
		}
		
		public IProject getProject() {
			return this.project;
		}
		
		public INamespaceDefinition getNamespaceDefinition() {
			return namespaceDefinition;
		}
	}
}
