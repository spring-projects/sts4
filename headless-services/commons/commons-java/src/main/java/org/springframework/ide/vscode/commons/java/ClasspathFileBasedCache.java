/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ClasspathFileBasedCache {

	private static final Gson gson = new Gson();
	private static final Logger log = LoggerFactory.getLogger(ClasspathFileBasedCache.class);

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
				gson.toJson(data, writer);
			} catch (IOException e) {
				log.error("Failed to write JSON data to " + file, e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						log.error("Failed to close file writer for file: " + file, e);
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
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(file);
				return gson.fromJson(fileReader, ClasspathData.class);
			} catch (Throwable e) {
				log.error("Failed to read JSON data from " + file, e);
			} finally {
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						log.error("Failed to close file reader for file: " + file, e);
					}
				}
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
