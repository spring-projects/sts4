package org.springframework.ide.vscode.util;

import java.util.Collection;

/**
 * @author Kris De Volder
 */
public class CollectionUtil {

	public static <E> boolean hasElements(Collection<E> c) {
		return c!=null && !c.isEmpty();
	}

}
