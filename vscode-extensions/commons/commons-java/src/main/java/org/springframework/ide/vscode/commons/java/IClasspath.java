package org.springframework.ide.vscode.commons.java;

import java.nio.file.Path;

public interface IClasspath {

	/**
	 * Deprecated: Remove this. We should expose here information about classpath as a list of entries. Not
	 * depend on where it is read from.
	 */
	@Deprecated
	Path getPath();

}
