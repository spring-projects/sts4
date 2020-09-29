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
package org.springsource.ide.eclipse.commons.core.process;

/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.5.2
 */
public class SystemErrOutputWriter implements OutputWriter {

	public void write(String line) {
		System.err.println(line);
	}

}
