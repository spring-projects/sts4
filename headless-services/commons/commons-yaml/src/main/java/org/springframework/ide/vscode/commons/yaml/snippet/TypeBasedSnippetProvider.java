/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.snippet;

import java.util.Collection;
import java.util.List;

import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypedProperty;

import com.google.common.collect.ImmutableList;

public interface TypeBasedSnippetProvider {

	Collection<Snippet> getSnippets(YType contextType);
	default Snippet getSnippet(YTypedProperty p) {
		return getSnippet(ImmutableList.of(p));
	}
	Snippet getSnippet(List<YTypedProperty> props);

}
