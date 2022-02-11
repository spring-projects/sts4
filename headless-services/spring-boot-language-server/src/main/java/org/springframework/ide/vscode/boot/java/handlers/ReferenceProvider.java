/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public interface ReferenceProvider {

	List<? extends Location> provideReferences(CancelChecker cancelToken, J node, Annotation annotation,
			FullyQualified type, int offset, TextDocument doc);

}
