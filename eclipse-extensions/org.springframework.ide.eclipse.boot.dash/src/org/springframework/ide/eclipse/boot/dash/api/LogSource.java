/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.api;

/**
 * If an {@link App} implements this interface then it represents a source of 'streamed' log
 * output. I.e. it generates output over time in background and this output can be
 * displayed in boot dash console ui.
 *
 * This interface is also used as a indicator whether open console action is applicable to an element.
 */
public interface LogSource {

	/**
	 * Called by boot dash to request a App that is a LogSource to start streaming
	 * log output to the console.
	 *
	 * @param logConsole the console to which output should be streamed.
	 * @param includeHistory when true, it means before streaming current logs the
	 *     app may also first send historic logs to the console. If an app doesn't
	 *     have the capability to provide historic logs, it can just ignore this
	 *     parameter.
	 * @return a LogConnection object which allows the boot dashboard to stop
	 *    the log streaming when the console is closed.
	 */
	LogConnection connectLog(AppConsole logConsole, boolean includeHistory);
}
