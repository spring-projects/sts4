package org.springframework.ide.vscode.commons.java;

import java.nio.file.Path;

public interface IJavaProject extends IJavaElement {
	IType findType(String fqName);
	Path getPath();
}
