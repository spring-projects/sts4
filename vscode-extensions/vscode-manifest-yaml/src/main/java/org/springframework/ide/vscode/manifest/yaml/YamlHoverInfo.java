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

public class YamlHoverInfo implements HoverInfo {

	@Override
	public String renderAsText() {
		return null;
	}

	@Override
	public String renderAsHtml() {
		return null;
	}

	@Override
	public String renderAsMarkdown() {
		return null;
	}

}
