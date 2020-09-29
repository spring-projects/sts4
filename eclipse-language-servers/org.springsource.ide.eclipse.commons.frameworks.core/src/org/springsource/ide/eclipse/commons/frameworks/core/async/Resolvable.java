/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.async;

/**
 * An object implementing this interface represents some entity awaiting
 * the result of an asynchronous computation. The result should
 * either be a value passed to the 'resolve' method (if the computation
 * succeeded) or an Exception, passed to the 'reject' method (if the
 * compuration failed).
 * <p>
 * Generally a computation can only complete by a call to resolve or
 * reject.
 * <p>
 * Implementations of Resolvable may rely on this assumption to free up
 * resources when resolve/reject are called.
 * <p>
 * Generally, implementations of {@link Resolvable} should guard against
 * bad/buggy callers and by disregarding additional calls to resolve/reject
 * after the first.
 *
 *
 * @author Kris De Volder
 */
public interface Resolvable<T> {
	void resolve(T value);
	void reject(Exception e);
}
