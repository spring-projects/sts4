package org.springframework.ide.vscode.util;

/**
 * Mimicks eclipse IRegion (i.e. a region is a offset + length).
 */
public interface IRegion {

	int getOffset();
	int getLength();

}
