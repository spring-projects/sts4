package org.springframework.ide.vscode.boot.java.annotations;

import java.util.Collection;
import java.util.function.Predicate;

record AnnotationTypeInformation(String fqn, Collection<String> inheritedAnnotations) {
	
	public boolean inherits(String fullyQualifiedAnnotationType) {
		return fqn.equals(fullyQualifiedAnnotationType) || inheritedAnnotations.contains(fullyQualifiedAnnotationType);
	}
	
	public boolean inherits(Predicate<String> annotationFqnTest) {
		if (annotationFqnTest.test(fqn)) {
			return true;
		}
		for (String fqn : inheritedAnnotations) {
			if (annotationFqnTest.test(fqn)) {
				return true;
			}
		}
		return false;
	}

}
