package org.springframework.tooling.jdt.ls.commons.classpath;

import java.util.List;

public class Classpath {
	
	public static final String ENTRY_KIND_SOURCE = "source";
	public static final String ENTRY_KIND_BINARY = "binary";
	public static final String OUTPUT_LOCATION = "output_location";

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
		public String toString() {
			return "CPE [kind=" + kind + ", path=" + path + "]\n";
		}

	}

}