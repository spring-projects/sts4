/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

/**
 * The interface that all those interested in receiving updates of the value of a Live expression 
 * should implement.
 * 
 * @author Kris De Volder
 */
@FunctionalInterface
public interface ValueListener<T> {
	void gotValue(LiveExpression<T> exp, T value);
}
