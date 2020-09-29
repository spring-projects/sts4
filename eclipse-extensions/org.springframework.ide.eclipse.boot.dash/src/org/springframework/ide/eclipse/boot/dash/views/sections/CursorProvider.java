/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Cursor;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Same idea as a 'LabelProvider' but provides mouse cursor for viewer cells.
 * <p>
 * Note that, typcually, a cursor provider might need to allocate Cursor objects.
 * In which case it is also responsible for disposing them. In that case,
 * it should also implement {@link Disposable}.
 *
 * @author Kris De Volder
 */
public interface CursorProvider {

	Cursor getCursor(ViewerCell cell);

}
