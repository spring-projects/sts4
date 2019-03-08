/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.java.properties.parser;

/**
 * Basic Parser interface
 *
 * @author Alex Boyko
 *
 */
public interface Parser {

	/**
	 * Parses passed in text based on Java Properties format
	 *
	 * @param text Text to parse
	 * @return Results of the parsing. See {@link ParseResults}
	 */
	ParseResults parse(String text);

}
