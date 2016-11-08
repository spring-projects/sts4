package org.springframework.ide.vscode.commons.java;

public interface IPrimitiveType extends IJavaType {
	
	static IPrimitiveType INT = () -> "I";
	
	static IPrimitiveType BOOLEAN = () -> "Z";
	
	static IPrimitiveType CHAR = () -> "C";
	
	static IPrimitiveType FLOAT = () -> "F";
	
	static IPrimitiveType BYTE = () -> "B";

	static IPrimitiveType DOUBLE = () -> "D";
	
	static IPrimitiveType LONG = () -> "J";
	
	static IPrimitiveType SHORT = () -> "S";
	
}
