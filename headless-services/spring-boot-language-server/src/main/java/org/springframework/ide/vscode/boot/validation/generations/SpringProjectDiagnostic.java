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
package org.springframework.ide.vscode.boot.validation.generations;

import java.net.URI;

import org.eclipse.lsp4j.Diagnostic;

public class SpringProjectDiagnostic {

	private final Diagnostic diagnostic;
	private final URI uri;

	public SpringProjectDiagnostic(Diagnostic diagnostic, URI uri) {
		this.diagnostic = diagnostic;
		this.uri = uri;
	}

	public Diagnostic getDiagnostic() {
		return diagnostic;
	}

	public URI getUri() {
		return uri;
	}
}
