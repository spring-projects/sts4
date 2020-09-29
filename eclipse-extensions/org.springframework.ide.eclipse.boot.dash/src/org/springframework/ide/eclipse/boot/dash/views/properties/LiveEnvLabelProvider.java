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
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.live.model.DisplayName;
import org.springsource.ide.eclipse.commons.livexp.ui.util.TreeElementWrappingContentProvider;

public class LiveEnvLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof TreeElementWrappingContentProvider.TreeNode) {
			return getText(((TreeElementWrappingContentProvider.TreeNode) element).getWrappedValue());
		} else if (element instanceof DisplayName) {
			return ((DisplayName) element).getDisplayName();
		}
		return super.getText(element);
	}

}