package org.springframework.ide.vscode.java;

public interface IAnnotation extends IJavaElement {

	/**
	 * Returns the member-value pairs of this annotation. Returns an empty
	 * array if this annotation is a marker annotation. Returns a size-1 array if this
	 * annotation is a single member annotation. In this case, the member
	 * name is always <code>"value"</code>.
	 *
	 * @return the member-value pairs of this annotation
	 */
	IMemberValuePair[] getMemberValuePairs();

}
