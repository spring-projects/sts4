/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util;

import java.util.Collection;

/**
 * @author Kris De Volder
 */
public class CollectionUtil {

	public static <E> boolean hasElements(Collection<E> c) {
		return c!=null && !c.isEmpty();
	}

}
