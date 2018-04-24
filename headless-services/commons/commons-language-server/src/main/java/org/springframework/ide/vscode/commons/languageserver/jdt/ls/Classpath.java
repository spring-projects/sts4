package org.springframework.ide.vscode.commons.languageserver.jdt.ls;


import java.util.List;

public class Classpath {

	public static final String ENTRY_KIND_SOURCE = "source";
	public static final String ENTRY_KIND_BINARY = "binary";

	private List<CPE> entries;
	private String defaultOutputFolder;

	public Classpath(List<CPE> entries, String defaultOutputFolder) {
		super();
		this.entries = entries;
		this.defaultOutputFolder = defaultOutputFolder;
	}

	public List<CPE> getEntries() {
		return entries;
	}

	public void setEntries(List<CPE> entries) {
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

	public static class CPE {
		private String kind;
		private String path;

		/**
		 * This only applies for 'source' entries.
		 */
		private String outputFolder;

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
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + ((outputFolder == null) ? 0 : outputFolder.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
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
			return true;
		}

		@Override
		public String toString() {
			return "CPE [kind=" + kind + ", path=" + path + ", outputFolder=" + outputFolder + "]";
		}
	}

	public static boolean isSource(CPE e) {
		return e!=null && Classpath.ENTRY_KIND_SOURCE.equals(e.getKind());
	}

}