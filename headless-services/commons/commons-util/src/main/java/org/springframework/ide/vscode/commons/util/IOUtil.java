/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

	/**
	 * Copy data from an inputstream into a file until end of the inputstream
	 * is reached.
	 * <p>
	 * The input stream is closed automatically.
	 */
	public static void pipe(InputStream data, File target) throws IOException {
		target.getParentFile().mkdirs(); //try to create dirs for parent if they don't exist.
		OutputStream out = new BufferedOutputStream(new FileOutputStream(target));
		try {
			pipe(data, out);
		} finally {
			out.close();
		}
	}

	/**
	 * Copy input stream to output stream until end of the inputstream is reached.
	 * The intpustream is closed automatically, but the output stream is not.
	 */
	public static void pipe(InputStream input, OutputStream output) throws IOException {
		try {
		    byte[] buf = new byte[1024*4];
		    int n = input.read(buf);
		    while (n >= 0) {
		      output.write(buf, 0, n);
		      n = input.read(buf);
		    }
		    output.flush();
		} finally {
			input.close();
		}
	}

	public static String toString(InputStream input) throws Exception {
		return toString(input, "UTF8");
	}

	private static String toString(InputStream input, String encoding) throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		pipe(input, buf);
		return buf.toString(encoding);
	}

	/**
	 * Sick and tired of writing try-catch around close calls... If something can't close, it usually means it
	 * was already closed, no longer exists etc. This method catches and ignores the exceptions.
	 */
	public static void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			//ignore
		}
	}

	public static byte[] toBytes(InputStream stream) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		pipe(stream, bytes);
		return bytes.toByteArray();
	}

}
