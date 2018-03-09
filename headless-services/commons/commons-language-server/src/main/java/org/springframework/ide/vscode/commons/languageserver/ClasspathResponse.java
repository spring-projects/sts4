/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver;

import java.util.List;

public class ClasspathResponse {

	public static final String ENTRY_KIND_SOURCE = "source";
	public static final String ENTRY_KIND_BINARY = "binary";

	private List<Entry> entries;
	private String defaultOutputFolder;

	public ClasspathResponse(List<Entry> entries, String defaultOutputFolder) {
		super();
		this.entries = entries;
		this.defaultOutputFolder = defaultOutputFolder;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	public String getDefaultOutputFolder() {
		return defaultOutputFolder;
	}

	public void setDefaultOutputFolder(String defaultOutputFolder) {
		this.defaultOutputFolder = defaultOutputFolder;
	}

	@Override
	public String toString() {
		return "Classpath [entries=" + entries + ", defaultOutputFolder=" + defaultOutputFolder + "]";
	}

	public static class Entry {

		private String kind;
		private String path;

		public Entry(String kind, String path) {
			super();
			this.kind = kind;
			this.path = path;
		}

		public String getKind() {
			return kind;
		}

		public void setKind(String kind) {
			this.kind = kind;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		@Override
		public String toString() {
			return "CPE [kind=" + kind + ", path=" + path + "]\n";
		}
	}
}