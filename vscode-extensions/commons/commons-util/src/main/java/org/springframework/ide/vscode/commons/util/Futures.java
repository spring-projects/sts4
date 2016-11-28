package org.springframework.ide.vscode.commons.util;

import java.util.concurrent.CompletableFuture;

public class Futures {

	/**
	 * Depcrecated. Use {@link CompletableFuture}.completedFuture() instead.
	 */
	@Deprecated
	public static <T> CompletableFuture<T> of(T value) {
		return CompletableFuture.completedFuture(value);
	}
	
}
