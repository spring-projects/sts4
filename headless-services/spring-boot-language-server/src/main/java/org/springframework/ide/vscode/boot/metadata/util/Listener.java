/*******************************************************************************
 * Copyright (c) 2014, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata.util;

/**
 * @author Kris De Volder
 */
@FunctionalInterface
public interface Listener<T> {

	void changed(T info);

}
