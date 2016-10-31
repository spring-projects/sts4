package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.Iterator;

/**
 * Utility to generate 'sort keys' in ascending order.
 * <p>
 * VSCode uses a String in each completion to determine the order of completions.
 * We use a 'score' based on how well a key matches what was typed.
 * <p>
 * To go from a 'score' to a sort-key we presort our proposals and then assign 
 * a sort key for vscode.
 */
public class SortKeys implements Iterator<String> {
	
	private int counter;
	
	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public String next() {
		return String.format("%05d", counter++);
	}

}
