/*******************************************************************************
 * Copyright (c) 2015, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public interface SpringPropertyIndexProvider {
	SpringPropertyIndex getIndex(IDocument doc);
	SpringPropertyIndex getIndex(IJavaProject project);

	void onChange(Runnable runnable);
}
