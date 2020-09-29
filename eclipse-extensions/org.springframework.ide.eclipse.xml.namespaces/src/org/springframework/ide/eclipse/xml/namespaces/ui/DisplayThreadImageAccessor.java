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
import org.eclipse.swt.widgets.Display;

/**
 * {@link IImageAccessor} that caches {@link Image} created by a call to {@link #createImage()}.
 * <p>
 * {@link #createImage()} is only called once and only from within the Display thread.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public abstract class DisplayThreadImageAccessor implements IImageAccessor {
	
	private Image image;
	
	/**
	 * {@inheritDoc}
	 */
	public final Image getImage() {
		if (image == null && Display.getCurrent() != null) {
			image = createImage();
		}
		return image;
	}
	
	protected abstract Image createImage();

}
