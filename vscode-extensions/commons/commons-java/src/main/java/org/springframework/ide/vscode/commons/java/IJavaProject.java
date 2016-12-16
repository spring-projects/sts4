package org.springframework.ide.vscode.commons.java;

import java.util.function.Predicate;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public interface IJavaProject extends IJavaElement {
	
	IType findType(String fqName);
	
	Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, Predicate<IType> typeFilter);
	
	Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm);
	
	Flux<IType> allSubtypesOf(IType type);
	
	IClasspath getClasspath();
}
