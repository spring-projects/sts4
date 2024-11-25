/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class RenderablesTest {
	
	@Test
	void escapeParenthesisForMardownLink() {
		Renderable r = Renderables.link("my-link-with-parenthesis", "https://foo.com/index(1).html");
		StringBuilder sb = new StringBuilder();
		r.renderAsMarkdown(sb);
		assertThat(sb.toString()).isEqualTo("[my-link-with-parenthesis](https://foo.com/index%281%29.html)");
	}

}
