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

import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * @author Kris De Volder
 */
public interface YTypedProperty {
	String getName();
	YType getType();
	Renderable getDescription();
	default boolean isRequired() { return false; }
	default boolean isDeprecated() { return false; }
	default String getDeprecationMessage() { return null; }
	default boolean isPrimary() { return false; }
}
