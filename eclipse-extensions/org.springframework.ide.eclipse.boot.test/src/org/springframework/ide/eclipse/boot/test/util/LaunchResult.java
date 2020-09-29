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
package org.springframework.ide.eclipse.boot.test.util;

public class LaunchResult {

	public final int terminationCode;
	public final String out;
	public final String err;

	LaunchResult(int terminationCode, String out, String err) {
		super();
		this.terminationCode = terminationCode;
		this.out = out;
		this.err = err;
	}
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("---- Sys.out ---\n");
		buf.append(out);
		buf.append("---- Sys.err ---\n");
		buf.append(err);
		return buf.toString();
	}
}