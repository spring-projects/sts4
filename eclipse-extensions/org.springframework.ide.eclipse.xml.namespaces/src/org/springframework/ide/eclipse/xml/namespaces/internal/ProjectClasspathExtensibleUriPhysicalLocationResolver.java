/*******************************************************************************
 * Copyright (c) 2013, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverExtension;
import org.springframework.ide.eclipse.xml.namespaces.ProjectAwareUrlStreamHandlerService;

/**
 * {@link URIResolverExtension} resolves the physical location of schema files on the project classpath using the
 * special project-aware protocol that {@link ProjectClasspathExtensibleUriResolver} uses.
 * 
 * @author Martin Lippert
 * @since 3.2.0
 */
@SuppressWarnings({ "restriction" })
public class ProjectClasspathExtensibleUriPhysicalLocationResolver implements URIResolverExtension {

	public ProjectClasspathExtensibleUriPhysicalLocationResolver() {
	}

	public String resolve(IFile file, String baseLocation, String publicId, String systemId) {
		if (systemId != null && systemId.startsWith(ProjectAwareUrlStreamHandlerService.PROJECT_AWARE_PROTOCOL_HEADER)) {
			String nameAndLocation = systemId.substring(ProjectAwareUrlStreamHandlerService.PROJECT_AWARE_PROTOCOL_HEADER.length());
			String realSystemId = nameAndLocation.substring(nameAndLocation.indexOf('/') + 1);
			return realSystemId;
		}
		return null;
	}

}
