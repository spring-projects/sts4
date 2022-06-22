/*******************************************************************************
 * Copyright (c) 2020 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.commands;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

//
// THIS IS A COPY OF THE NEW FILLLAYOUTFACTORY CLASS THAT DOESN'T EXIST YET
// IN ECLIPSE 2022-03 RELEASE.
//
// This copy serves as a fill-in for as long as we support 2022-03.
//


/**
 * FillLayoutFactory creates and initializes {@link FillLayout fill layouts}. It
 * is used as a shorthand for writing "<code>new FillLayout()</code>" and
 * initializing some fields. In this case the main benefit is a more concise
 * syntax and the ability to create more than one identical {@link FillLayout}
 * from the same factory. Changing a property of the factory will affect future
 * layouts created by the factory, but has no effect on layouts that have
 * already been created.
 *
 * @since 3.26
 */
public final class FillLayoutFactory {

	/**
	 * Template layout. The factory will create copies of this layout.
	 */
	private FillLayout layout;

	/**
	 * Creates a factory that creates copies of the given layout.
	 *
	 * @return a new FillLayoutFactory instance with a new FillLayout
	 */
	public static FillLayoutFactory fillDefaults() {
		return new FillLayoutFactory(copyLayout(new FillLayout()));
	}

	/**
	 * Creates a new FillLayoutFactory that will create copies of the given layout.
	 *
	 * @param l layout to copy
	 */
	private FillLayoutFactory(FillLayout l) {
		this.layout = l;
	}

	/**
	 * Creates a factory that creates copies of the given layout.
	 *
	 * @param l layout to copy
	 * @return a new FillLayoutFactory instance that creates copies of the given
	 *         layout
	 */
	public static FillLayoutFactory createFrom(FillLayout l) {
		return new FillLayoutFactory(copyLayout(l));
	}


	/**
	 * Creates a copy of the receiver.
	 *
	 * @return a copy of the receiver
	 */
	public FillLayoutFactory copy() {
		return new FillLayoutFactory(create());
	}


	/**
	 * Sets the margins for layouts created with this factory. The margins
	 * are the distance between the outer cells and the edge of the layout.
	 *
	 * @param margins margin size (pixels)
	 * @return this
	 */
	public FillLayoutFactory margins(Point margins) {
		layout.marginWidth = margins.x;
		layout.marginHeight = margins.y;
		return this;
	}

	/**
	 * Sets the margins for layouts created with this factory. The margins are the
	 * distance between the outer cells and the edge of the layout.
	 *
	 * @param width  margin width (pixels)
	 * @param height margin height (pixels)
	 * @return this
	 */
	public FillLayoutFactory margins(int width, int height) {
		layout.marginWidth = width;
		layout.marginHeight = height;
		return this;
	}

	/**
	 * Sets the margins for layouts created with this factory. The margins specify
	 * the number of pixels of width and height of the layout. Note that these
	 * margins will be added to the ones specified by {@link #margins(int, int)}.
	 *
	 * @param width  width margin size (pixels)
	 * @param height height margin size (pixels)
	 * @return this
	 *
	 * @since 3.3
	 */
	public FillLayoutFactory extendedMargins(int width, int height) {
		layout.marginWidth = layout.marginWidth + width;
		layout.marginHeight = layout.marginHeight + height;
		return this;
	}


	/**
	 * Creates a new {@code FillLayout}, and initializes it with values from the
	 * factory.
	 *
	 * @return a new initialized {@code FillLayout}.
	 * @see #applyTo
	 */
	public FillLayout create() {
		return copyLayout(layout);
	}

	/**
	 * Creates a new {@code FillLayout} and attaches it to the given composite. Does
	 * not create the layout data of any of the controls in the composite.
	 *
	 * @param c composite whose layout will be set
	 * @see #create
	 * @see FillLayoutFactory
	 */
	public void applyTo(Composite c) {
		c.setLayout(copyLayout(layout));
	}

	/**
	 * Copies the given {@code FillLayout} instance
	 *
	 * @param l layout to copy
	 * @return a new FillLayout
	 */
	public static FillLayout copyLayout(FillLayout l) {
		FillLayout result = new FillLayout();
		result.marginHeight = l.marginHeight;
		result.marginWidth = l.marginWidth;
		result.spacing = l.spacing;

		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FillLayout()\n"); //$NON-NLS-1$
		if (layout.marginWidth != 0 || layout.marginHeight != 0) {
			builder.append("    .margins("); //$NON-NLS-1$
			builder.append(layout.marginWidth);
			builder.append(", "); //$NON-NLS-1$
			builder.append(layout.marginHeight);
			builder.append(")\n"); //$NON-NLS-1$
		}
		return builder.toString();
	}
}
