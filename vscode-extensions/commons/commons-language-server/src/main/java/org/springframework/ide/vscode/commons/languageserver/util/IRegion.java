package org.springframework.ide.vscode.commons.languageserver.util;

/**
 * Mimicks eclipse IRegion (i.e. a region is a offset + length).
 */
public interface IRegion {

	int getOffset();
	int getLength();

}
