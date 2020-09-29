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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.plugins;

/**
 * Represents a column in a TreeViewer, where name is the column header name,
 * and width the initial length of the column
 * @author Nieraj Singh
 */
public interface IPluginListColumn {

	public String getName();

	public int getWidth();

}
