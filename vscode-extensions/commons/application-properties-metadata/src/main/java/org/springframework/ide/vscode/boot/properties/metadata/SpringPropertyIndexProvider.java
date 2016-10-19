/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.properties.metadata;

import org.springframework.ide.vscode.boot.properties.util.FuzzyMap;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;


@FunctionalInterface
public interface SpringPropertyIndexProvider {
	FuzzyMap<PropertyInfo> getIndex(IDocument doc);
}
