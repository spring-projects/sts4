package org.springframework.ide.vscode.commons.util;

public interface IRequestor<T> {
	void accept(T node);
}
