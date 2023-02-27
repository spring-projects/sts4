/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public interface JavaReconciler {
	
	void reconcile(IJavaProject project, IDocument doc, IProblemCollector problemCollector);

	Map<IDocument, Collection<ReconcileProblem>> reconcile(IJavaProject project, List<TextDocument> docs, Runnable incrementProgress);
	
	default int getTotalWorkUnits(List<TextDocument> docs) {
		return 0;
	}

}
