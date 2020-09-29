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
package org.springframework.ide.eclipse.editor.support.util;

import org.eclipse.core.resources.IContainer;

/**
 * To make code that pops up dialogs and such more testable, this interface
 * can be implemented providing either 'real' or 'mock' implementations.
 *
 * @author Kris De Volder
 */
public interface UserInteractions {
	IContainer chooseOneSourceFolder(String title, String message, IContainer[] options, IContainer preferred);
	void error(String title, String message);
}
