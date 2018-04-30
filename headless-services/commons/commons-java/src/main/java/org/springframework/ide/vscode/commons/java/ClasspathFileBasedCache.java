/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.ide.vscode.commons.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClasspathFileBasedCache {

	public static final ClasspathFileBasedCache NULL = new ClasspathFileBasedCache(null);

	public static final String CLASSPATH_DATA_CACHE_FILE = "classpath-data.json";

	final private File file;

	public ClasspathFileBasedCache(File file) {
		super();
		this.file = file;
	}

	public synchronized void persist(ClasspathData data) {
		if (file != null && data != null) {
			FileWriter writer = null;
			try {
				Files.createDirectories(file.getParentFile().toPath());
				writer = new FileWriter(file);
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(writer, data);
			} catch (IOException e) {
				Log.log(e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						Log.log(e);
					}
				}
			}
		}
	}

	public boolean isCached() {
		return file != null && file.exists();
	}

	public synchronized ClasspathData load() {
		if (file != null && file.exists()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.readValue(file, ClasspathData.class);
			} catch (Throwable e) {
				Log.log(e);
			}
		}
		return ClasspathData.EMPTY_CLASSPATH_DATA;
	}


	public void delete() {
		if (file != null && file.exists()) {
			file.delete();
		}
	}

}
