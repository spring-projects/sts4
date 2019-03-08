/*******************************************************************************
 * Copyright (c) 2000, 2009, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pivotal Inc     - Copied and modified.
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

public interface IMemberValuePair {

	/**
	 * Returns the member's name of this member-value pair.
	 *
	 * @return the member's name of this member-value pair.
	 */
	String getMemberName();

	/**
	 * Returns the value of this member-value pair. The type of this value
	 * is function of this member-value pair's {@link #getValueKind() value kind}. It is an
	 * instance of {@link Object}[] if the value is an array.
	 * <p>
	 * If the value kind is {@link #K_UNKNOWN} and the value is not an array, then the
	 * value is <code>null</code>.
	 * If the value kind is {@link #K_UNKNOWN} and the value is an array, then the
	 * value is an array containing {@link Object}s and/or <code>null</code>s for
	 * unknown elements.
	 * See {@link #K_UNKNOWN} for more details.
	 * </p>
	 * @return the value of this member-value pair.
	 */
	Object getValue();

}
