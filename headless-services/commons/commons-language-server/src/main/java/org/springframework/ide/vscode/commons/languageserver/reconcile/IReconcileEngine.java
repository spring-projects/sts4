/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.reconcile;

import org.springframework.ide.vscode.commons.util.text.IDocument;

public interface IReconcileEngine {
	IReconcileEngine NULL = (d, p) -> {
		p.beginCollecting();
		p.endCollecting();
	};

	public void reconcile(IDocument doc, IProblemCollector problemCollector);
}
