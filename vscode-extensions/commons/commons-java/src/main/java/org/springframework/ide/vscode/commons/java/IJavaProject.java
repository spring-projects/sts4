package org.springframework.ide.vscode.commons.java;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public interface IJavaProject extends IJavaElement {

	/**
	 * TypeFilter is deprecated. Should use java.util.funcion.Predicate<IType> instead.
	 */
	@Deprecated
	@FunctionalInterface
	public static interface TypeFilter {
		boolean accept(IType type);
	}
	
	IType findType(String fqName);
	
	Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, TypeFilter typeFilter);
	
	Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm);
	
	Flux<IType> allSubtypesOf(IType type);
	
	IClasspath getClasspath();
}
