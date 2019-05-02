/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class Classpath {

	public static final String ENTRY_KIND_SOURCE = "source";
	public static final String ENTRY_KIND_BINARY = "binary";
	public static final Classpath EMPTY = new Classpath(Collections.<CPE>emptyList());

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
		private boolean isOwn = false;
		private boolean isTest = false;
		private boolean isJavaContent = false;

		public String getOutputFolder() {
			return outputFolder;
		}

		public void setOutputFolder(String outputFolder) {
			this.outputFolder = outputFolder;
		}

		public CPE() {}

		public CPE(String kind, String path) {
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

		public boolean isOwn() {
			return isOwn;
		}

		public void setOwn(boolean isOwn) {
			this.isOwn = isOwn;
		}

		public boolean isTest() {
			return isTest;
		}

		public void setTest(boolean isTest) {
			this.isTest = isTest;
		}

		public boolean isJavaContent() {
			return isJavaContent;
		}

		public void setJavaContent(boolean isJavaContent) {
			this.isJavaContent = isJavaContent;
		}

		@Override
		public String toString() {
			return "CPE [kind=" + kind + ", path=" + path + ", outputFolder=" + outputFolder + ", sourceContainerUrl="
					+ sourceContainerUrl + ", javadocContainerUrl=" + javadocContainerUrl + ", isSystem=" + isSystem
					+ ", isOwn=" + isOwn + ", isTest=" + isTest + ", isJavaContent=" + isJavaContent + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (isJavaContent ? 1231 : 1237);
			result = prime * result + (isOwn ? 1231 : 1237);
			result = prime * result + (isSystem ? 1231 : 1237);
			result = prime * result + (isTest ? 1231 : 1237);
			result = prime * result + ((javadocContainerUrl == null) ? 0 : javadocContainerUrl.hashCode());
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + ((outputFolder == null) ? 0 : outputFolder.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + ((sourceContainerUrl == null) ? 0 : sourceContainerUrl.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CPE other = (CPE) obj;
			if (isJavaContent != other.isJavaContent)
				return false;
			if (isOwn != other.isOwn)
				return false;
			if (isSystem != other.isSystem)
				return false;
			if (isTest != other.isTest)
				return false;
			if (javadocContainerUrl == null) {
				if (other.javadocContainerUrl != null)
					return false;
			} else if (!javadocContainerUrl.equals(other.javadocContainerUrl))
				return false;
			if (kind == null) {
				if (other.kind != null)
					return false;
			} else if (!kind.equals(other.kind))
				return false;
			if (outputFolder == null) {
				if (other.outputFolder != null)
					return false;
			} else if (!outputFolder.equals(other.outputFolder))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			if (sourceContainerUrl == null) {
				if (other.sourceContainerUrl != null)
					return false;
			} else if (!sourceContainerUrl.equals(other.sourceContainerUrl))
				return false;
			return true;
		}

	}

	public static boolean isSource(CPE e) {
		return e!=null && Classpath.ENTRY_KIND_SOURCE.equals(e.getKind());
	}

	public static boolean isBinary(CPE e) {
		return e!=null && Classpath.ENTRY_KIND_BINARY.equals(e.getKind());
	}
	
	public static boolean isProjectSource(CPE e) {
		return isSource(e) && e.isOwn();
	}
	
	public static boolean isProjectJavaSource(CPE cpe) {
		return isProjectSource(cpe) && cpe.isJavaContent();
	}
	
	public static boolean isProjectNonTestJavaSource(CPE cpe) {
		return isProjectJavaSource(cpe) && !cpe.isTest();
	}
	
	public static boolean isProjectTestJavaSource(CPE cpe) {
		return isProjectJavaSource(cpe) && cpe.isTest();
	}


}