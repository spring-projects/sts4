/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.manifest.yaml;

import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfo;
import org.springframework.ide.vscode.commons.languageserver.hover.IHoverEngine;
import org.springframework.ide.vscode.commons.languageserver.util.IDocument;

public class ManifestYmlHoverEngine implements IHoverEngine {

	@Override
	public HoverInfo getHover(IDocument document, int offset) throws Exception {
		// TODO. Create the hover info based on AST
		HoverInfo info = new YamlHoverInfo();

		return info;
	}

}
