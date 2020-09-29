/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.ui;

import org.eclipse.swt.graphics.Image;

/**
 * Implementations of this interface can load {@link Image} instances.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public interface IImageAccessor {
	
	/**
	 * Returns an image.
	 */
	Image getImage();
}