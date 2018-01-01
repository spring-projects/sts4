package org.test;

import java.util.function.Function;

public class ScannedFunctionClass implements Function<String, String> {

	@Override
	public String apply(String t) {
		return t.toUpperCase();
	}

}
