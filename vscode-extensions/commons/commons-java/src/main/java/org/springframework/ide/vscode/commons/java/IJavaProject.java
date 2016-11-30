package org.springframework.ide.vscode.commons.java;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public interface IJavaProject extends IJavaElement {
	
	@FunctionalInterface
	public static interface TypeFilter {
		boolean accept(IType type);
	}
	
	IType findType(String fqName);
	
	Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, TypeFilter typeFilter);
	
	Flux<IType> allSubtypesOf(IType type);
	
	IClasspath getClasspath();
}
