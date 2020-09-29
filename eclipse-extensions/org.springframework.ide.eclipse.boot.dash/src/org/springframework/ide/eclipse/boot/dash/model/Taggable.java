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
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.LinkedHashSet;

/**
 * Interface for taggable items
 *
 * @author Alex Boyko
 *
 */
public interface Taggable {

	/**
	 * Returns an ordered set of string tags
	 * @return array of tags
	 */
	LinkedHashSet<String> getTags();


	/**
	 * Sets an ordered set of new tags
	 * @param newTags new tags
	 */
	void setTags(LinkedHashSet<String> newTags);

}
