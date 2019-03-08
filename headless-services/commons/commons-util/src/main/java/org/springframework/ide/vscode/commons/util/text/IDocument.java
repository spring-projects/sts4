/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util.text;

import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.commons.util.BadLocationException;

public interface IDocument {

	String getUri();
	String get();
	IRegion getLineInformationOfOffset(int offset);
	int getLength();
	String get(int start, int len) throws BadLocationException;
	int getNumberOfLines();
	String getDefaultLineDelimiter();
	char getChar(int offset) throws BadLocationException;
	int getLineOfOffset(int offset) throws BadLocationException;
	IRegion getLineInformation(int line);
	int getLineOffset(int line) throws BadLocationException;
	void replace(int start, int len, String text) throws BadLocationException;
	String textBetween(int start, int end) throws BadLocationException;
	LanguageId getLanguageId();
	int getVersion();
	Range toRange(IRegion asRegion) throws BadLocationException;

}
