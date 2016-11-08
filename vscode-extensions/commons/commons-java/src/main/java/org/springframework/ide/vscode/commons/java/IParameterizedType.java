package org.springframework.ide.vscode.commons.java;

import java.util.stream.Stream;

public interface IParameterizedType extends IJavaType {
	
	IJavaType owner();
	
	Stream<IJavaType> arguments();

}
