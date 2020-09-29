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
package org.springsource.ide.eclipse.commons.core.pstore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kris De Volder
 */
public class InMemoryPropertyStore implements IPropertyStore {

	private Map<String, String> props = new HashMap<>();

	@Override
	public synchronized String get(String key) {
		return props.get(key);
	}

	@Override
	public synchronized void put(String key, String value) throws Exception {
		props.put(key, value);
	}

}
