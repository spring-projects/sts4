package org.springframework.ide.vscode.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link IRequestor} that simplies stores all items received into
 * a List
 *
 * @author Kris De Volder
 */
public class Collector<T> implements IRequestor<T> {

	@SuppressWarnings("unchecked")
	private List<T> nodes = Collections.EMPTY_LIST;

	@Override
	public void accept(T node) {
		if (nodes==Collections.EMPTY_LIST) {
			nodes = new ArrayList<T>();
		}
		nodes.add(node);
	}

	public List<T> get() {
		return nodes;
	}
}
