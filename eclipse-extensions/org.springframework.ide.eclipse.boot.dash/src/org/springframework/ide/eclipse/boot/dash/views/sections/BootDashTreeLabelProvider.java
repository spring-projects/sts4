/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.jface.viewers.ColumnViewer;
import org.springframework.ide.eclipse.boot.dash.views.BootDashCellLabelProvider;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

/**
 * @author Kris De Volder
 */
public class BootDashTreeLabelProvider extends BootDashCellLabelProvider {

	public BootDashTreeLabelProvider(Stylers stylers, ColumnViewer tv) {
		//TODO: refactor so we do not need this 'dummy' column?
		super(tv, BootDashColumn.TREE_VIEWER_MAIN/*dummy*/, stylers);
	}
}
