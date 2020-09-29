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
package org.springsource.ide.eclipse.commons.livexp.util;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.google.common.collect.ImmutableList;

/**
 * General interface for filtering elements. With some provisions for
 * using them as filter in some ui (e.g. so matching elements can be highlighted somehow).
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public interface Filter<T> {
	boolean accept(T t);
	default boolean isTrivial() { return false; }
	default	Iterable<IRegion> getHighlights(String text) { 
		return isTrivial() 
				? ImmutableList.of()
				: ImmutableList.of(new Region(0, text.length())); 
	}
}
