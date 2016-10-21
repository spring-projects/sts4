package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;

import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.HtmlSnippet;

public class JavaProjectWithClasspathFile implements IJavaProject {

	private File cpFile;

	public JavaProjectWithClasspathFile(File cpFile) {
		this.cpFile = cpFile;
	}

	@Override
	public String getElementName() {
		return cpFile.getParentFile().getName();
	}

	@Override
	public HtmlSnippet getJavaDoc() {
		return null;
	}

	@Override
	public boolean exists() {
		return cpFile.exists();
	}

	@Override
	public IType findType(String fqName) {
		//TODO: implement
		return null;
	}

	@Override
	public IClasspath getClasspath() {
		return () -> cpFile.toPath();
	}

	@Override
	public String toString() {
		return "JavaProjectWithClasspathFile("+cpFile+")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cpFile == null) ? 0 : cpFile.hashCode());
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
		JavaProjectWithClasspathFile other = (JavaProjectWithClasspathFile) obj;
		if (cpFile == null) {
			if (other.cpFile != null)
				return false;
		} else if (!cpFile.equals(other.cpFile))
			return false;
		return true;
	}


}