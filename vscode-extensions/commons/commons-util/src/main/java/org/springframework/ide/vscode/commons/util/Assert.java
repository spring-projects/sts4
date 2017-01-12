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

public class Assert {

	public static void isNull(String msg, Object obj) {
		if (obj!=null) {
			throw new IllegalStateException(msg);
		}
	}

	public static void isLegal(boolean b) {
		if (!b) {
			throw new IllegalStateException();
		}
	}

	public static void isLegal(boolean b, String msg) {
		if (!b) {
			throw new IllegalStateException(msg);
		}
	}

	public static void isNotNull(Object it) {
		if (it==null) {
			throw new NullPointerException();
		}
	}

	public static void isTrue(boolean b) {
		isLegal(b);
	}

}
