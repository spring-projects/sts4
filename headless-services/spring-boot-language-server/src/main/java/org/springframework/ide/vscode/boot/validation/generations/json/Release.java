package org.springframework.ide.vscode.boot.validation.generations.json;

import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;

public class Release {
	
	private String version;
	private String status;
	private boolean current;
	
	public Version getVersion() {
		return SpringProjectUtil.getVersion(version);
	}
	
	public String getStatus() {
		return status;
	}
	
	public boolean isCurrent() {
		return current;
	}
}
