/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deprecated, this class is here to make porting old STS code easier. Code should
 * avoid using this as much as possible and replaces calls to this by using 
 * {@link java.util.logging.Logger} directly
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

}
