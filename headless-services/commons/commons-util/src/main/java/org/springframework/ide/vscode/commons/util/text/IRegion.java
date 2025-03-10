/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util.text;

/**
 * Mimicks eclipse IRegion (i.e. a region is a offset + length).
 */
public interface IRegion {

	int getOffset();
	int getLength();
	default int getEnd() {
		return getOffset() + getLength();
	}
	default int getStart() {
		return getOffset();
	}

}
