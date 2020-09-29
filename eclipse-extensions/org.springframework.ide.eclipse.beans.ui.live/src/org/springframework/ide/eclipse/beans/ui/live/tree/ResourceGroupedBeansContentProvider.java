/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.tree;

import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;

/**
 * Content provider for beans grouped by resources
 * 
 * @author Alex Boyko
 *
 */
public final class ResourceGroupedBeansContentProvider extends AbstractLiveBeansTreeContentProvider {
	
	public static final ResourceGroupedBeansContentProvider INSTANCE = new ResourceGroupedBeansContentProvider();
	
	private ResourceGroupedBeansContentProvider() {}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof LiveBeansModel) {
			LiveBeansModel model = (LiveBeansModel) inputElement;
			return model.getBeansByResource().toArray();
		}
		return new Object[0];
	}

}
