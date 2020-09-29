/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 */
package org.springframework.ide.eclipse.boot.dash.api;

/**
 * 'App' that can support the setting of system properties to be passed into theit JVM process
 * implement this interface to define how to inject these properties into the process
 * when it is launched.
 */
public interface SystemPropertySupport extends SystemPropertyProvider {

	/**
	 * Set property to a value or set it to null to 'erase' the property.
	 */
	void setSystemProperty(String name, String value);

}
