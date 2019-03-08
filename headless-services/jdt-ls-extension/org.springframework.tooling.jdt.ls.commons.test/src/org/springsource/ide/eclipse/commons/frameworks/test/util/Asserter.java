/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.test.util;

/**
 * An {@link Asserter} provides a single method which is expected
 * to check some stuff during a test (i.e. typically execute a bunch
 * of asserts).
 *
 * @author Kris De Volder
 */
@FunctionalInterface
public interface Asserter {
	/**
	 * Called on to check some conditions during a test. Should return
	 * normally to indicate 'success' and throw an exception of any type
	 * to indicate 'failure'.
	 */
	void execute() throws Exception;
}
