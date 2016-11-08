package org.springframework.ide.vscode.commons.jandex;

import org.jboss.jandex.UnresolvedTypeVariable;
import org.springframework.ide.vscode.commons.java.IUnresolvedTypeVariable;

final class UnresolvedTypeVariableWrapper extends TypeWrapper<UnresolvedTypeVariable> implements IUnresolvedTypeVariable {
	
	UnresolvedTypeVariableWrapper(UnresolvedTypeVariable type) {
		super(type);
	}

	@Override
	public String name() {
		return getType().name().toString();
	}
	
}
