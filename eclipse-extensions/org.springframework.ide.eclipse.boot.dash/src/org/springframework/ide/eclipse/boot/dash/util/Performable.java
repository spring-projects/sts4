/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * Like {@link Runnable} called in a 'ui' context, it also
 * allows throwing Exceptions (for the convenience of implementors).
 * <p>
 * It is the responsibility of callers to deal with the exceptions
 * (e.g. by logging problems or showing an error popup).
 */
public interface Performable {
	void perform(UserInteractions ui) throws Exception;
}
