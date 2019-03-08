/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is here to make porting old STS code easier. Deprecated: Instead of using this,
 * consider using {@link org.slf4j.Logger} directly. This allows for more fine-grained
 * tuning of logging levels per class.
 */
@Deprecated
public class Log {

	final static Logger logger = LoggerFactory.getLogger(Log.class);

	public static void log(Throwable e) {
		logger.error("Error", e);
	}

	public static void log(String message, Throwable t) {
		logger.error(message, t);
	}

	public static void log(String message) {
		logger.error(message);
	}

	public static void info(String info) {
		logger.info(info);
	}

	public static void warn(String string) {
		logger.warn(string);
	}

	/**
	 * Note: to enable debug output set this in launchconfig: -Dorg.slf4j.simpleLogger.log.org.springframework.ide.vscode.commons.util.Log=debug
	 */
	public static void debug(String string) {
		logger.debug(string);
	}

	public static void warn(String msg, Throwable e) {
		logger.warn(msg, e);
	}

}
