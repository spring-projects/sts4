package org.springframework.ide.eclipse.boot.dash.api;

import org.springframework.ide.eclipse.boot.dash.model.ClasspathPropertyTester;

public interface ClasspathBearing extends App {
	boolean hasClasspathProperty(ClasspathPropertyTester tester);
}
