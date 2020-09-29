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
package org.springsource.ide.eclipse.commons.core;

import java.util.List;

/**
 * @author Christian Dupuis
 * @author Kris De Volder
 * @since 2.5.0
 */
public interface ICommandHistory {

	/**
	 * The default value for maxSize.
	 */
	int DEFAULT_MAX_SIZE = 100;

	void add(Entry entry);

	void clear();

	Entry getLast();

	List<Entry> getRecentValid(int limit);

	boolean isEmpty();

	int size();

	Iterable<Entry> validEntries();

}
