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
package org.springframework.ide.vscode.commons.yaml.path;

import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNode;

/**
 * Different types of things (e.g. {@link ApplicationYamlAssistContext}, {@link SNode} ...) can
 * be traversed interpeting {@link YamlPath} as 'navigation operations'. To facilitate
 * 'reusable' traversal code, they can implement this interface.
 */
public interface YamlNavigable<T> {
	T traverse(YamlPathSegment s) throws Exception;
}
