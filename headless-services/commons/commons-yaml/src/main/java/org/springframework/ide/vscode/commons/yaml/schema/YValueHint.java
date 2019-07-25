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
package org.springframework.ide.vscode.commons.yaml.schema;

import org.springframework.ide.vscode.commons.languageserver.util.PlaceHolderString;
import org.springframework.ide.vscode.commons.util.Renderable;

public interface YValueHint {
	String getValue();
	String getLabel();

	/**
	 * Returns an optional extra text to insert after the value, for a completion.
	 * If non-null value is returned, then it will be inserted after the value,
	 * on the next line and indented to line-up relative to the indentation of
	 * the line where the value itself is being inserted.
	 */
	PlaceHolderString getExtraInsertion();

	/**
	 * An optional documentation string (i.e. shown in javadoc side hover in Eclipse style
	 * or in the line below a completion item in vscode style.
	 */
	Renderable getDocumentation();
}