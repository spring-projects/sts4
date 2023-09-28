/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;

public class Lsp4jUtils {
	
	public static Position getPositionAtEndOfEdit(TextEdit edit) {
		int numberOfLinebreaks = numberOfLineBreaks(edit.getNewText());
		return new Position(
				edit.getRange().getStart().getLine() + numberOfLinebreaks,
				numberOfLinebreaks == 0 ? edit.getRange().getStart().getCharacter() + edit.getNewText().length() :
					lengthOfLastLine(edit.getNewText())
		);
	}
	
	private static int numberOfLineBreaks(String s) {
		int numOfLinebreaks = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '\n') {
				numOfLinebreaks++;
			}
		}
		return numOfLinebreaks;
	}
	
	private static int lengthOfLastLine(String s) {
		int idx = s.lastIndexOf('\n');
		if (idx >= 0) {
			return s.length() - idx - 1;
		} else {
			return s.length();
		}
	}

}
