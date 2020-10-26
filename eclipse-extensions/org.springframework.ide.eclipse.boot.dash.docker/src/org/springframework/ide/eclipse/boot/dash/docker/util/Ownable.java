package org.springframework.ide.eclipse.boot.dash.docker.util;

/**
 * Represents a references that is either 'borrowed' or 'owned'.
 */
public class Ownable<T> {
	
	public final T ref;
	public final boolean isOwned;

	private Ownable(T ref, boolean owned) {
		this.ref = ref;
		this.isOwned = owned;
	}

	public static <T> Ownable<T> owned(T x) {
		return x == null ? null : new Ownable<T>(x, true);
	}

	public static <T> Ownable<T> borrowed(T x) {
		return x == null ? null : new Ownable<T>(x, false);
	}

}
