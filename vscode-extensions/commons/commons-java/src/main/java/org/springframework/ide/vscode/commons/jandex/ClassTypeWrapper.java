package org.springframework.ide.vscode.commons.jandex;

import org.jboss.jandex.ClassType;
import org.springframework.ide.vscode.commons.java.IClassType;

final class ClassTypeWrapper extends TypeWrapper<ClassType> implements IClassType {
	
	ClassTypeWrapper(ClassType type) {
		super(type);
	}

	@Override
	public String name() {
		return getType().name().toString();
	}

}
