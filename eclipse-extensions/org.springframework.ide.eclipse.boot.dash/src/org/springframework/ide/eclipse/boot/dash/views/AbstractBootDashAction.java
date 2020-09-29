/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.EnumSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * @author Kris De Volder
 */
public class AbstractBootDashAction extends Action implements Disposable {

	public static enum Location {
		CONTEXT_MENU,
		CUSTOMIZE_MENU
	}

	private boolean isVisible = true;
	protected final SimpleDIContext context;

	public EnumSet<Location> showIn() {
		return EnumSet.of(Location.CONTEXT_MENU);
	}

	protected AbstractBootDashAction(SimpleDIContext context, int style) {
		super("", style);
		this.context = context;
		context.assertDefinitionFor(UserInteractions.class);
	}

	protected AbstractBootDashAction(SimpleDIContext context) {
		this(context, IAction.AS_UNSPECIFIED);
	}

	public void dispose() {
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean show) {
		this.isVisible = show;
	}

	public UserInteractions ui() {
		return context.getBean(UserInteractions.class);
	}

}
