package org.springframework.ide.vscode.commons.java;

public interface IArrayType extends IJavaType {
	
	int dimensions();
	
	IJavaType component(); 

}
