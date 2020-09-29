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
 * @author Nieraj Singh
 */
public class Icon implements IIcon {

	private String iconLocation;

	public Icon(String iconLocation) {
		this.iconLocation = iconLocation;
	}

	public String getIconLocation() {
		return iconLocation;
	}

}
