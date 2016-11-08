package org.springframework.ide.vscode.commons.jandex;

import static org.springframework.ide.vscode.commons.jandex.Wrappers.wrap;
import java.util.stream.Stream;

import org.jboss.jandex.ParameterizedType;
import org.springframework.ide.vscode.commons.java.IJavaType;
import org.springframework.ide.vscode.commons.java.IParameterizedType;

final class ParameterizedTypeWrapper extends TypeWrapper<ParameterizedType> implements IParameterizedType {
	
	ParameterizedTypeWrapper(ParameterizedType type) {
		super(type);
	}

	@Override
	public String name() {
		return getType().name().toString();
	}

	@Override
	public IJavaType owner() {
		return wrap(getType().owner());
	}

	@Override
	public Stream<IJavaType> arguments() {
		return getType().arguments().stream().map(Wrappers::wrap);
	}
	
}
