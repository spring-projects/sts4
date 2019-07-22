/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that behaves just like "/dev/null" on Unix. I.e. any output
 * written to it is silently discarded.
 */
public class NullOutputStream extends OutputStream {

	@Override
	public void write(int b) throws IOException {
	}
	
	@Override
	public void write(byte[] b) throws IOException {
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
	}

}
