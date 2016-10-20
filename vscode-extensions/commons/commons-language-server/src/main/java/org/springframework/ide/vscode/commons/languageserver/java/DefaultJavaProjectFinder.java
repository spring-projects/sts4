package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.util.HtmlSnippet;
import org.springframework.ide.vscode.commons.util.StringUtil;

public class DefaultJavaProjectFinder implements JavaProjectFinder {

	private final String CLASSPATH_FILE_NAME;

	public DefaultJavaProjectFinder(String classpathFileName) {
		this.CLASSPATH_FILE_NAME = classpathFileName;
	}

	private File findClasspathFile(File file) {
		if (file!=null && file.exists()) {
			File cpFile = new File(file, CLASSPATH_FILE_NAME);
			if (cpFile.isFile()) {
				return cpFile;
			} else {
				return findClasspathFile(file.getParentFile());
			}
		}
		return null;
	}

	@Override
	public IJavaProject find(IDocument d) {
		String uriStr = d.getUri();
		if (StringUtil.hasText(uriStr)) {
			try {
				URI uri = new URI(uriStr);
				//TODO: This only work with File uri. Should it work with others too?
				File file = new File(uri).getAbsoluteFile();
				File cpFile = findClasspathFile(file);
				if (cpFile!=null) {
					return new JavaProjectWithClasspathFile(cpFile);
				}
			} catch (URISyntaxException | IllegalArgumentException e) {
				//garbage data. Ignore it.
			}
		}
		return null;
	};

	private static class JavaProjectWithClasspathFile implements IJavaProject {

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
		public Path getPath() {
			return cpFile.getParentFile().toPath();
		}

		@Override
		public String toString() {
			return "JavaProjectWithClasspathFile("+cpFile+")";
		}
	}
}
