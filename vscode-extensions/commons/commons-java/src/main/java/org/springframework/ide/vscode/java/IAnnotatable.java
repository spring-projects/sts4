package org.springframework.ide.vscode.java;

public interface IAnnotatable extends IJavaElement {

	IAnnotation[] getAnnotations();

}
