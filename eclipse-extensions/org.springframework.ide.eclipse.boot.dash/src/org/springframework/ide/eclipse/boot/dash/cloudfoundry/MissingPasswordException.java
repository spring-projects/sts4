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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

/**
 * Exception for missing password
 *
 * @author Alex Boyko
 *
 */
public class MissingPasswordException extends Exception {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;

	public MissingPasswordException(String message) {
		super(message);
	}

}
