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
package org.springframework.ide.vscode.boot.java.embadded.lang;

import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class AntlrUtils {

	public static Stream<Token> getAllLeafs(ParserRuleContext ctx) {
		if (ctx.children == null) {
			return Stream.empty();
		}
		return ctx.children.stream().flatMap(n -> {
			if (n instanceof ParserRuleContext prc) {
				return getAllLeafs(prc);
			} else if (n instanceof TerminalNode tn) {
				return Stream.of(tn.getSymbol());
			}
			return Stream.empty();
		});
	}

}
