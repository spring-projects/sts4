/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.java;

import org.openrewrite.Tree;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.Range;

public class RangeScopedJavaIsoVisitor<P> extends JavaIsoVisitor<P> {
	
	private Range range;
	
	public RangeScopedJavaIsoVisitor(Range range) {
		this.range = range;
	}

	@Override
	public @Nullable J visit(@Nullable Tree tree, P ctx) {
		if (tree instanceof J) {
			J t = (J) tree;
			Range r = t.getMarkers().findFirst(Range.class).orElse(null);
			if (range == null || r == null || r.getStart().getOffset() <= range.getStart().getOffset() && range.getEnd().getOffset() - 1 <= r.getEnd().getOffset()) {
				return (J) super.visit(t, ctx);
			} else {
				return t;
			}
		}
		return super.visit(tree, ctx);
	}

}
