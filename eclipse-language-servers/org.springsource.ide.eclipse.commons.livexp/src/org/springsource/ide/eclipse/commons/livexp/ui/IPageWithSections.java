/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstraction for the 'owner' of a UI 'Section' on a some page. This is so that we can
 * reuse UI widgetry more easily across different kinds of UI contexts (e.g. both on
 * a preferences page and a launch config editor.
 *
 * @author Kris De Volder
 */
public interface IPageWithSections {

	Shell getShell();
	IRunnableContext getRunnableContext();

}
