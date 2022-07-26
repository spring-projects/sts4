package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.jdt.core.IClasspathEntry;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;

import com.google.common.base.Predicate;

public interface ClasspathPropertyTester {
	String getId();
	boolean test(IClasspathEntry[] classpath);

	static final ClasspathPropertyTester HAS_DEVTOOLS = anyElement("HAS_DEVTOOLS", BootPropertyTester::isDevtoolsJar);
	static final ClasspathPropertyTester HAS_ACTUATORS = anyElement("HAS_ACTUATORS", BootPropertyTester::isActuatorJar);

	static ClasspathPropertyTester anyElement(String id, Predicate<IClasspathEntry> entryTester) {
		return new ClasspathPropertyTester() {

			@Override
			public boolean test(IClasspathEntry[] classpath) {
				for (IClasspathEntry e : classpath) {
					if (entryTester.test(e)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String getId() {
				return id;
			}

			@Override
			public String toString() {
				return "ClasspathPropertyTester("+id+")";
			}
		};
	}
}
