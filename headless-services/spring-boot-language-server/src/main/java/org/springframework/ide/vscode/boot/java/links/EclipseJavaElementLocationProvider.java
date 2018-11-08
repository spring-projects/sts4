/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.links;

import java.net.URI;

import org.eclipse.lsp4j.Location;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMember;

public class EclipseJavaElementLocationProvider implements JavaElementLocationProvider {

	@Override
	public Location findLocation(IJavaProject project, IMember member) {
		URI uri = EclipseSourceLinks.eclipseIntroUri(project, member);
		return uri == null ? null : new Location(uri.toString(), null);
	}

}
