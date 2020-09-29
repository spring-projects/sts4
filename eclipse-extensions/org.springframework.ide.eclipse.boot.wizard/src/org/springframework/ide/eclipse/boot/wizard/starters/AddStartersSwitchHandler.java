/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

public class AddStartersSwitchHandler extends AbstractHandler implements IHandler {

	private static boolean addStartersSwitch = "true".equalsIgnoreCase(System.getProperty("org.springframework.ide.eclipse.boot.wizard.starters"));

	public static boolean isAddStartersEnabled() {
		return addStartersSwitch;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		addStartersSwitch = !addStartersSwitch;
		return null;
	}

}
