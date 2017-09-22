/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemTypes;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;


/**
 * @author Martin Lippert
 */
public class BootJavaReconcileEngine implements IReconcileEngine {

	private static final Pattern infoWord = Pattern.compile("information|warning");

	private static final ProblemType INFO_AVAILABLE = ProblemTypes.create("BootInfoAvailable", ProblemSeverity.INFO);
	private static final ProblemType WARNING = ProblemTypes.create("BootInfoAvailable", ProblemSeverity.WARNING);

	@Override
	public void reconcile(IDocument _doc, IProblemCollector problemCollector) {
		problemCollector.beginCollecting();
		DocumentRegion doc = new DocumentRegion(_doc);
		try {
			Matcher matcher = infoWord.matcher(doc);
			while (matcher.find()) {
				problemCollector.accept(info(doc.subSequence(matcher.start(), matcher.end())));
			}
		} finally {
			problemCollector.endCollecting();
		}
	}

	private ReconcileProblem info(DocumentRegion region) {
		if (region.toString().contains("info")) {
			return new ReconcileProblemImpl(INFO_AVAILABLE, "There's dynamic boot info available", region.getStart(), region.getLength());
		}
		return new ReconcileProblemImpl(WARNING, "Be warned!", region.getStart(), region.getLength());
	}
}
