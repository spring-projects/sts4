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
package org.springframework.ide.eclipse.editor.support.yaml.schema;

import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;

/**
 * @author Kris De Volder
 */
public interface YTypedProperty {
	String getName();
	YType getType();
	HtmlSnippet getDescription();
	boolean isDeprecated();
	String getDeprecationMessage();
}
