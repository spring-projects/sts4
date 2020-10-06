package org.springframework.ide.vscode.xml;

import java.util.Collection;
import java.util.function.Consumer;

import org.springframework.ide.vscode.commons.protocol.java.Classpath;

public interface IJavaProjectProvider {
	
	interface IJavaProjectData {
		
		String getName();
		
		String getUri();
		
		Classpath getClasspath();
		
	}
	
	Collection<IJavaProjectData> all();
	
	IJavaProjectData get(String name);
	
	void addListener(Consumer<IJavaProjectData> listener);
	
	void removeListener(Consumer<IJavaProjectData> listener);
	
	default IJavaProjectData findProject(String fileUri) {
		IJavaProjectData bestMatch = null;
		for (IJavaProjectData p : all()) {
			if (fileUri.startsWith(p.getUri())) {
				if (bestMatch == null) {
					bestMatch = p;
				} else {
					if (bestMatch.getUri().length() < p.getUri().length()) {
						bestMatch = p;
					}
				}
			}
		}
		return bestMatch;
	}

	default boolean exists(String name) {
		return get(name) != null;
	}

}
