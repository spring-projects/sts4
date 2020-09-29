/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation.framework;

import org.eclipse.core.resources.IResource;

/**
 * Abstraction for a 'element' that a validation rule can operate on. Typically this represents
 * some type of view on an underlying resource. For example if the resource is a .java file then
 * the view may be an object that implements an interface specific to inspecting a java Compilation unit.
 */
public interface IModelElement {

	IResource getElementResource();

}
