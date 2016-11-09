package org.springframework.ide.vscode.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deprecated, this class is here to make porting old STS code easier. Code should
 * avoid using this as much as possible and replaces calls to this by using Slf4J loggers
 * directly.
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
