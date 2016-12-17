package org.springframework.ide.vscode.commons.yaml.util;

import java.util.stream.Stream;

public class Streams {
	
	/**
	 * Like Stream.of but returns Stream.empty of the element is null
	 */
	public static <T> Stream<T> of(T e) {
		return e==null ? Stream.empty() : Stream.of(e);
	}

}
