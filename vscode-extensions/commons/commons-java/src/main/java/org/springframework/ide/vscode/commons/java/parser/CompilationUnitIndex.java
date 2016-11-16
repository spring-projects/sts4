package org.springframework.ide.vscode.commons.java.parser;

import java.net.URL;

import com.github.javaparser.ast.CompilationUnit;

public interface CompilationUnitIndex {
	
	static final CompilationUnitIndex DEFAULT = new DefaultCompilationUnitIndex();
	
	CompilationUnit getCompilationUnit(URL url); 

}
