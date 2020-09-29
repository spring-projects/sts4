/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.ui;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;

/**
 * Interface describing a XSD namespace declaration.
 * @author Christian Dupuis
 * @since 2.0
 */
public interface INamespaceDefinition {

	@Deprecated
	String getNamespacePrefix();

	String getNamespacePrefix(IResource resource);
	
	String getDefaultNamespacePrefix();

	String getNamespaceURI();

	@Deprecated
	String getDefaultSchemaLocation();
	
	String getDefaultSchemaLocation(IResource resource);
	
	Set<String> getSchemaLocations();
	
	Image getNamespaceImage();

}
