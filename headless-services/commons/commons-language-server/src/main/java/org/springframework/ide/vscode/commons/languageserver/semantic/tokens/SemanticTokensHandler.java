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
package org.springframework.ide.vscode.commons.languageserver.semantic.tokens;

import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensDelta;
import org.eclipse.lsp4j.SemanticTokensDeltaParams;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public interface SemanticTokensHandler {
	
	SemanticTokensWithRegistrationOptions getCapability();
	
	default SemanticTokens semanticTokensFull(SemanticTokensParams params, CancelChecker cancelChecker) {
		return new SemanticTokens();
	}

	default Either<SemanticTokens, SemanticTokensDelta> semanticTokensFullDelta(SemanticTokensDeltaParams params, CancelChecker cancelChecker) {
		return Either.forLeft(new SemanticTokens());
	}

	default SemanticTokens semanticTokensRange(SemanticTokensRangeParams params, CancelChecker cancelChecker) {
		return new SemanticTokens();
	}

}
