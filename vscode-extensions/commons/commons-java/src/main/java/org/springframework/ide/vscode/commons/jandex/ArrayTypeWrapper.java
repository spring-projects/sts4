package org.springframework.ide.vscode.commons.jandex;

import static org.springframework.ide.vscode.commons.jandex.Wrappers.wrap;

import org.jboss.jandex.ArrayType;
import org.springframework.ide.vscode.commons.java.IArrayType;
import org.springframework.ide.vscode.commons.java.IJavaType;

final class ArrayTypeWrapper extends TypeWrapper<ArrayType> implements IArrayType {
	
	ArrayTypeWrapper(ArrayType type) {
		super(type);
	}

	@Override
	public String name() {
		return getType().name().toString();
	}

	@Override
	public int dimensions() {
		return getType().dimensions();
	}

	@Override
	public IJavaType component() {
		return wrap(getType().component());
	}
	
}
