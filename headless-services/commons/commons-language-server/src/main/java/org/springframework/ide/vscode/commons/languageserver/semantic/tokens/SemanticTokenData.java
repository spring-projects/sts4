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

import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.Region;

public record SemanticTokenData(
		IRegion range,
		String type,
		String[] modifiers
	) implements Comparable<SemanticTokenData> {
	
	public SemanticTokenData(int start, int end, String type, String[] modifiers) {
		this(new Region(start, end -start), type, modifiers);
	}

	@Override
	public int compareTo(SemanticTokenData o) {
		if (range.getOffset() == o.range().getOffset()) {
			return range.getLength() - o.range().getLength();
		}
		return range.getOffset() - o.range().getOffset();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(modifiers);
		result = prime * result + Objects.hash(range, type);
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
		return range.getLength() == other.range.getLength() && Arrays.equals(modifiers, other.modifiers) && range.getOffset() == other.range().getOffset()
				&& Objects.equals(type, other.type);
	}
	
	public int getStart() {
		return range.getStart();
	}
	
	public int getEnd() {
		return range.getEnd();
	}
	
}
