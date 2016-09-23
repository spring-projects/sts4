package org.springframework.ide.vscode.util;

/**
 * Requestor that remembers only the last item received.
 *
 * @author Kris De Volder
 */
public class RememberLast<T> implements IRequestor<T> {

	private T last = null;

	@Override
	public void accept(T node) {
		this.last = node;
	}

	/**
	 * @return the last received item, may return null if no items where received.
	 */
	public T get() {
		return last;
	}

}
