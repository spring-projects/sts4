/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.ui.launch;

import org.eclipse.swt.widgets.Display;

/**
 * Abstract subclass of Runable that executes its code in the UiThread.
 */
public abstract class UiRunnable implements Runnable {

	/**
	 * This method is final. Implement 'uiRun' instead.
	 */
	public final void run() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				uiRun();
			}
		});
	}

	protected abstract void uiRun();

	protected Display getDisplay() {
		return Display.getDefault();
	}

}
