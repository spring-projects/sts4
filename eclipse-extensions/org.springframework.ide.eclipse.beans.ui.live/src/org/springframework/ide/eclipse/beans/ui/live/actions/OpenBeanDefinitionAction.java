/*******************************************************************************
 * Copyright (c) 2012, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.actions;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.utils.LiveBeanUtil;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class OpenBeanDefinitionAction extends AbstractOpenResourceAction {

	public OpenBeanDefinitionAction() {
		super("Open Bean Definition File");
	}
	
	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		List<?> elements = selection.toList();
		for (Object obj : elements) {
			if (obj instanceof LiveBean) {
				LiveBean bean = (LiveBean) obj;
				LiveBeanUtil.navigateToResource(bean);
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			List<?> elements = selection.toList();
			for (Object obj : elements) {
				if (obj instanceof LiveBean) {
					LiveBean bean = (LiveBean) obj;
					String resource = bean.getResource();
					if (resource != null && resource.trim().length() > 0 && !resource.equalsIgnoreCase("null")) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
