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

/**
 * An instance of this provides a way to store and retrieve
 * properties. The properties are persisted somehow.
 * <p>
 * This interface is deliberately kept very sparse so as to be
 * 'easy' to implement. Its an interface meant for implementors.
 * <p>
 * If you are a client you can an wrap instance of this interface
 * into a {@link PropertyStoreApi} for a more convenient api
 * with more operations.
 *
 * @author Kris De Volder
 */
public interface IPropertyStore {
	String get(String key);
	void put(String key, String value) throws Exception;
}