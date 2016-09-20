package org.springframework.ide.vscode.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListenerList<T> {
	
	private List<Consumer<T>> listeners = new ArrayList<>();

	public synchronized void fire(T evt) {
		for (Consumer<T> l : listeners) {
			l.accept(evt);
		}
	}

	public void add(Consumer<T> l) {
		listeners.add(l);
	}

}
