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

import java.util.Collections;
import java.util.List;

public interface SemanticTokensDataProvider {
	
	List<String> getTokenTypes();
	default List<String> getTypeModifiers() { return Collections.emptyList(); }
	List<SemanticTokenData> computeTokens(String text, int initialOffset);

}
