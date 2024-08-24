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

import java.util.Arrays;
import java.util.Objects;

public record SemanticTokenData(
		int start,
		int end,
		String type,
		String[] modifiers
	) implements Comparable<SemanticTokenData> {

	@Override
	public int compareTo(SemanticTokenData o) {
		if (start == o.start) {
			return end - o.end;
		}
		return start - o.start;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(modifiers);
		result = prime * result + Objects.hash(end, start, type);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SemanticTokenData other = (SemanticTokenData) obj;
		return end == other.end && Arrays.equals(modifiers, other.modifiers) && start == other.start
				&& Objects.equals(type, other.type);
	}
	
}
