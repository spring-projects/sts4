/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.rewrite.java;

import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Space;
import org.openrewrite.java.tree.Space.Location;
import org.openrewrite.marker.Markers;

public class JavaMarkerVisitor<P> extends JavaIsoVisitor<P> {

	@Override
	public Space visitSpace(Space space, Location loc, P p) {
		return space;
	}

	@Override
	public Markers visitMarkers(Markers markers, P p) {
		return markers;
	}

}
