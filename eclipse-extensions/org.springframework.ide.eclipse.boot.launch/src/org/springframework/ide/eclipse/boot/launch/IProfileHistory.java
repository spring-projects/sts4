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
package org.springframework.ide.eclipse.boot.launch;

import org.eclipse.core.resources.IProject;

/**
 * Provides some means to retrieve profile history for projects. I.e. a list of
 * spring boot profiles that have been used to launch a given project in the past.
 *
 * @author Kris De Volder
 */
public interface IProfileHistory {

	String[] getHistory(IProject value);

}
