/*******************************************************************************
 * Copyright (c) 2013, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.actions;

import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public abstract class AbstractOpenResourceAction extends BaseSelectionListenerAction {
	
	protected AbstractOpenResourceAction(String text) {
		super(text);
	}

	protected boolean hasTypeInProject(TypeLookup workspaceContext, String className) {
		return workspaceContext.findType(className) != null;
	}

}
