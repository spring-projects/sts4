package org.springframework.ide.vscode.boot.java.annotations;

import java.util.Collection;
import java.util.function.Predicate;

record AnnotationTypeInformation(String bindingKey, Collection<String> inheritedAnnotations) {
	
	public boolean inherits(String annotationBindingKey) {
		return bindingKey.equals(annotationBindingKey) || inheritedAnnotations.contains(annotationBindingKey);
	}
	
	public boolean inherits(Predicate<String> annotationBindingKeyTest) {
		if (annotationBindingKeyTest.test(bindingKey)) {
			return true;
		}
		for (String fqn : inheritedAnnotations) {
			if (annotationBindingKeyTest.test(fqn)) {
				return true;
			}
		}
		return false;
	}

}
