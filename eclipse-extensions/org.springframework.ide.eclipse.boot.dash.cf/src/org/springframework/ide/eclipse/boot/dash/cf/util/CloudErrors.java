/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.util;

public class CloudErrors {

	/**
	 * check if access token error
	 *
	 * @param e
	 * @return true if access token error. False otherwise
	 */
	public static boolean isAccessTokenError(Exception e) {
		return hasCloudError(e, "access_denied") || hasCloudError(e, "Error requesting access token")
				|| (hasCloudError(e, "access") && hasCloudError(e, "token"));
	}

	public static boolean isBadRequest(Exception e) {
		return hasCloudError(e, "400");
	}

	/**
	 * check 404 error. For example, application does not exist
	 *
	 * @param t
	 * @return true if 404 error. False otherwise
	 */
	public static boolean isNotFoundException(Exception e) {
		return hasCloudError(e, "404");
	}

	/**
	 * check 503 service error.
	 *
	 * @param t
	 * @return true if 404 error. False otherwise
	 */
	public static boolean is503Error(Exception e) {
		return hasCloudError(e, "503");
	}

	public static boolean hasCloudError(Exception e, String error) {
		return hasError(e, error);
	}

	public static void checkAndRethrowCloudException(Exception e, String errorPrefix) throws Exception {
		// Special case for CF exceptions:
		// CF exceptions may not contain the error in the message but rather
		// the description
		throw e;
	}

	protected static boolean hasError(Exception exception, String pattern) {
		String message = exception.getMessage();
		return message != null && message.contains(pattern);
	}

}
