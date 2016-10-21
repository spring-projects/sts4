package org.springframework.ide.vscode.commons.java;

public interface IJavaProject extends IJavaElement {
	IType findType(String fqName);
	IClasspath getClasspath();
}
