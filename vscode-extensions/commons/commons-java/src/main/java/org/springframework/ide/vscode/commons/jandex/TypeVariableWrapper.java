package org.springframework.ide.vscode.commons.jandex;

import org.jboss.jandex.TypeVariable;
import org.springframework.ide.vscode.commons.java.ITypeVariable;

final class TypeVariableWrapper extends TypeWrapper<TypeVariable> implements ITypeVariable {
	
	TypeVariableWrapper(TypeVariable type) {
		super(type);
	}

	@Override
	public String name() {
		return getType().name().toString();
	}
	
}
