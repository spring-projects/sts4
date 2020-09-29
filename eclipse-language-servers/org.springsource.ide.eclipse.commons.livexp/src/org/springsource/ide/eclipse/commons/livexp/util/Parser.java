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

/**
 * A parser converts a textual representation into an object of some type.
 *
 * @author Kris De Volder
 */
public interface Parser<T> {
	T parse(String text);

	/**
	 * The 'identity' function can be used as trivial Parser<String>
	 */
	public static final Parser<String> IDENTITY = new Parser<String>() {
		public String parse(String text) {
			return text;
		};
	};
}
