/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.springframework.ide.vscode.commons.protocol.spring.AbstractSpringIndexElement;

public class WebfluxRouteElementRangesIndexElement extends AbstractSpringIndexElement {

	private static final Range[] NO_RANGES = new Range[0];

	private Range[] ranges;

	public WebfluxRouteElementRangesIndexElement(Range... ranges) {
		this.ranges = ranges != null ? ranges : NO_RANGES;
	}

	public Range[] getRanges() {
		return ranges;
	}

	public boolean contains(Position position) {
		for (Range range : ranges) {
			if (isEqualOrBefore(range.getStart(), position) && isEqualsOrBehind(range.getEnd(), position)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * returns true if position1 is the same or before position2 in a document
	 */
	protected boolean isEqualOrBefore(Position position1, Position position2) {
		if (position1.getLine() < position2.getLine()) return true;
		if (position1.getLine() == position2.getLine() && position1.getCharacter() <= position2.getCharacter()) return true;
		return false;
	}

	/**
	 * returns true if position1 is the same or behind position2 in a document
	 */
	protected boolean isEqualsOrBehind(Position position1, Position position2) {
		if (position1.getLine() > position2.getLine()) return true;
		if (position1.getLine() == position2.getLine() && position1.getCharacter() >= position2.getCharacter()) return true;
		return false;
	}

}
