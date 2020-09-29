/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.test.util;

import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot;

/**
 * @author Kris De Volder
 * @author Andrew Eisenberg
 */
public class WidgetIsDisposed extends DefaultCondition {

	private AbstractSWTBot<? extends Widget> widgetToDispose;
	private boolean isDisposed = false;

	public WidgetIsDisposed(AbstractSWTBot<? extends Widget> widget) {
		this.widgetToDispose = widget;
	}

	public boolean test() throws Exception {
		bot.getDisplay().syncExec(new Runnable() {
			public void run() {
				isDisposed = widgetToDispose.widget.isDisposed();
			}
		});
		return isDisposed;
	}
	public String getFailureMessage() {
		return "We expected "+widgetToDispose+" to be disposed, bit it didn't happen";
	}

}
