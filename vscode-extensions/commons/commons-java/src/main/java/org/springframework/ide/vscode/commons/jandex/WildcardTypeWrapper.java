package org.springframework.ide.vscode.commons.jandex;

import org.jboss.jandex.WildcardType;
import org.springframework.ide.vscode.commons.java.IWildcardType;

final class WildcardTypeWrapper extends TypeWrapper<WildcardType> implements IWildcardType {
	
	WildcardTypeWrapper(WildcardType type) {
		super(type);
	}

	@Override
	public String name() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
