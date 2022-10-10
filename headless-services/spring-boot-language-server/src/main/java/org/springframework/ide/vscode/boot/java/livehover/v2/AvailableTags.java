package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.Arrays;

/**
 * @author Udayani V
 */
public class AvailableTags {
	
	private String tag;
	private String[] values;
	
	public String getTag() {
		return tag;
	}

	public String[] getValues() {
		return values;
	}	

	@Override
	public String toString() {
		return "AvailableTags [tag=" + tag + ", values=" + Arrays.toString(values) + "]";
	}

}
