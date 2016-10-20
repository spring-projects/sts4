package org.springframework.ide.vscode.commons.util;

import javax.inject.Provider;

/**
 * An abstract class implementing {@link Provider}. The provided value
 * is computed on demand and cached once computed.
 * <p>
 * Subclass must implement the compute method.
 */
public abstract class LazyProvider<T> implements Provider<T> {

	private boolean computed = false;
	private T cached = null;

	@Override
	public synchronized final T get() {
		if (!computed) {
			cached = compute();
		}
		return cached;
	}

	protected abstract T compute();

}
