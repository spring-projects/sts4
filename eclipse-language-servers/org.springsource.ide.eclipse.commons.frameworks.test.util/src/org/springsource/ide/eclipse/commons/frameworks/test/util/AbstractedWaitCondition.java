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

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

/**
 * @author Kris De Volder
 */
public abstract class AbstractedWaitCondition implements ICondition {
	protected SWTBot bot;

	protected AbstractedWaitCondition(SWTBot bot) {
		this.bot = bot;
	}

	public void init(SWTBot bot) {
		//
	}

	public void waitForTest() {
		bot.waitUntil(this);
	}

	public void waitForTest(long timeout) {
		bot.waitUntil(this, timeout);
	}
}
