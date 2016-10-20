package org.springframework.ide.vscode.commons.util;

import java.util.concurrent.CompletableFuture;

public class Futures {

	public static <T> CompletableFuture<T> of(T value) {
		CompletableFuture<T> f = new CompletableFuture<T>();
		f.complete(value);
		return f;
	}
	
}
