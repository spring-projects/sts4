package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
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
	}
}
