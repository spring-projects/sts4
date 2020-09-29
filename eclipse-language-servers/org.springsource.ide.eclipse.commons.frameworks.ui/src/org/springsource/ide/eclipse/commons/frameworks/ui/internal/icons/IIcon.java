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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.icons;

/**
 * Allows elements, like tree viewer or Project explorer elements, to have icons
 * and be handled by a framework icon manager
 * @author Nieraj Singh
 */
public interface IIcon {

	/**
	 * The icon location should be an full file system path or a platform URI
	 * pointing to an icon image file. (e.g.,
	 * "platform:/plugin/org.grails.ide.eclipse.explorer/icons/full/obj16/controllers.gif"
	 * )
	 * 
	 * @return
	 */
	public String getIconLocation();

}
