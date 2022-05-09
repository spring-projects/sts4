package org.springframework.ide.vscode.boot.java.livehover.v2;

public enum ProcessType {
	LOCAL,
	REMOTE;

	String jsonName() {
		return name().toLowerCase();
	}
}
