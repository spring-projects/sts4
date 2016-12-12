package org.springframework.ide.vscode.commons.util.text;

/**
 * Mimicks eclipse IRegion (i.e. a region is a offset + length).
 */
public interface IRegion {

	int getOffset();
	int getLength();

}
