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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.ide.vscode.commons.util.Log;

public class ClasspathFileBasedCache {
	public static final String CLASSPATH_DATA_CACHE_FILE = "classpath-data.json";
	
	private static final String OUTPUT_FOLDER_PROPERTY = "outputFolder";
	private static final String CLASSPATH_RESOURCES_PROPERTY = "classpathResources";
	private static final String CLASSPATH_ENTRIES_PROPERTY = "classpathEntries";
	private static final String NAME_PROPERTY = "name";
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
				JSONObject json = new JSONObject();
				json.put(NAME_PROPERTY, data.name);
				json.put(CLASSPATH_ENTRIES_PROPERTY, data.classpathEntries.stream().map(e -> e.toString()).collect(Collectors.toList()));
				json.put(CLASSPATH_RESOURCES_PROPERTY, data.classpathResources);
				json.put(OUTPUT_FOLDER_PROPERTY, data.outputFolder);
				writer = new FileWriter(file);
				json.write(writer);
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
			try {
				JSONObject json = new JSONObject(new JSONTokener(new FileInputStream(file)));
				String name = json.getString(NAME_PROPERTY);
				JSONArray classpathEntriesJson = json.optJSONArray(CLASSPATH_ENTRIES_PROPERTY);
				JSONArray classpathResourcesJson = json.optJSONArray(CLASSPATH_RESOURCES_PROPERTY);
				String outputFolderStr = json.optString(OUTPUT_FOLDER_PROPERTY);
				
				return new ClasspathData(
						name,
						classpathEntriesJson == null ? Collections.emptySet() : classpathEntriesJson.toList().stream()
								.filter(o -> o instanceof String)
								.map(o -> (String) o)
								.map(s -> new File(s).toPath())
								.collect(Collectors.toSet()),
						classpathResourcesJson == null ? Collections.emptySet() : classpathResourcesJson.toList().stream()
								.filter(o -> o instanceof String)
								.map(o -> (String) o)
								.collect(Collectors.toSet()),
						outputFolderStr == null ? null : new File(outputFolderStr).toPath()
					);
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
