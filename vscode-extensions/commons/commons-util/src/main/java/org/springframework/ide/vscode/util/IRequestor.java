package org.springframework.ide.vscode.util;

public interface IRequestor<T> {
	void accept(T node);
}
