package org.springframework.ide.vscode.commons.java;

import java.util.stream.Stream;

public interface IAnnotatable extends IJavaElement {

	Stream<IAnnotation> getAnnotations();

}
