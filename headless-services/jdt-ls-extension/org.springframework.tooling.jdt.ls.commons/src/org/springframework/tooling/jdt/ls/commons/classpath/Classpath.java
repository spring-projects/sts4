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
package org.springframework.tooling.jdt.ls.commons.classpath;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class Classpath {

	public static final String ENTRY_KIND_SOURCE = "source";
	public static final String ENTRY_KIND_BINARY = "binary";

	private List<CPE> entries;

	public Classpath(List<CPE> entries) {
		super();
		this.entries = entries;
	}

	public List<CPE> getEntries() {
		return entries;
	}

	public void setEntries(List<CPE> entries) {
		this.entries = entries;
	}

	@Override
	public String toString() {
		return "Classpath [entries=" + entries + "]";
	}

	public static class CPE {

		// TODO: it seems like a good idea to make all classpath entries the same in that they all have
		// - a place with source code
		// - a place with compiled code
		// - a place with java doc
		// So it seems like we should be able to chnage this so that the same named attribute is used
		// in both cases (rather then one be 'getOutputFolder' and one 'getPath' to obtain location of the compiled code).

		private String kind;

		private String path; // TODO: Change to File, Path or URL?
		private String outputFolder;
		private URL sourceContainerUrl;
		private URL javadocContainerUrl;
		private boolean isSystem = false;

		public String getOutputFolder() {
			return outputFolder;
		}

		public void setOutputFolder(String outputFolder) {
			Assert.isLegal(new File(outputFolder).isAbsolute());
			this.outputFolder = outputFolder;
		}

		public CPE() {}

		private CPE(String kind, String path) {
			super();
			this.kind = kind;
			setPath(path);
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
			Assert.isLegal(path == null || new File(path).isAbsolute());
			this.path = path;
		}

		public URL getJavadocContainerUrl() {
			return javadocContainerUrl;
		}

		public void setJavadocContainerUrl(URL javadocContainerUrl) {
			this.javadocContainerUrl = javadocContainerUrl;
		}

		public URL getSourceContainerUrl() {
			return sourceContainerUrl;
		}

		public void setSourceContainerUrl(URL sourceContainerUrl) {
			this.sourceContainerUrl = sourceContainerUrl;
		}

		public static CPE binary(String path) {
			return new CPE(ENTRY_KIND_BINARY, path);
		}

		public static CPE source(File sourceFolder, File outputFolder) {
			CPE cpe = new CPE(ENTRY_KIND_SOURCE, sourceFolder.getAbsolutePath());
			cpe.setOutputFolder(outputFolder.getAbsolutePath());
			return cpe;
		}

		public boolean isSystem() {
			return isSystem;
		}

		public void setSystem(boolean isSystem) {
			this.isSystem = isSystem;
		}

		@Override
		public String toString() {
			return "CPE [kind=" + kind + ", path=" + path + "]";
		}
	}

	public static boolean isSource(CPE e) {
		return e!=null && Classpath.ENTRY_KIND_SOURCE.equals(e.getKind());
	}

	public static boolean isBinary(CPE e) {
		return e!=null && Classpath.ENTRY_KIND_BINARY.equals(e.getKind());
	}

}