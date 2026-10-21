/*******************************************************************************
 * Copyright (c) 2015, 2026 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import java.util.Collection;
import java.util.function.Consumer;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public interface SpringPropertyIndexProvider {
	SpringPropertyIndex getIndex(IDocument doc);
	SpringPropertyIndex getIndex(IJavaProject project);

	/**
	 * Registers a listener that is called with the projects whose property index changed.
	 * Multiple listeners are supported.
	 */
	void onChange(Consumer<Collection<IJavaProject>> listener);
}
