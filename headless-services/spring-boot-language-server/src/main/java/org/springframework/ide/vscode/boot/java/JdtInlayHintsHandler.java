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
package org.springframework.ide.vscode.boot.java;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.springframework.ide.vscode.boot.java.reconcilers.CompositeASTVisitor;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.InlayHintHandler;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class JdtInlayHintsHandler implements InlayHintHandler {
	
	private final CompilationUnitCache cuCache;
	private final JavaProjectFinder projectFinder;
	private final Collection<JdtInlayHintsProvider> inlayHintsProviders;

	public JdtInlayHintsHandler(CompilationUnitCache cuCache, JavaProjectFinder projectFinder, Collection<JdtInlayHintsProvider> inlayHintsProviders) {
		this.cuCache = cuCache;
		this.projectFinder = projectFinder;
		this.inlayHintsProviders = inlayHintsProviders;
	}

	@Override
	public List<InlayHint> handle(TextDocument doc, Range range, CancelChecker cancelChecker) {
		Optional<IJavaProject> optProject = projectFinder.find(doc.getId());
		if (optProject.isPresent()) {
			IJavaProject jp = optProject.get();
			List<JdtInlayHintsProvider> applicableInlayHintsProviders = inlayHintsProviders.stream().filter(tp -> tp.isApplicable(jp)).collect(Collectors.toList());
			if (!applicableInlayHintsProviders.isEmpty()) {
				return cuCache.withCompilationUnit(jp, URI.create(doc.getUri()), cu -> computeInlayHints(applicableInlayHintsProviders, jp, cu, range, doc));
			}
		}
		return Collections.emptyList();
	}

	private List<InlayHint> computeInlayHints(List<JdtInlayHintsProvider> applicableInlayHintsProviders, IJavaProject jp, CompilationUnit cu, Range r, TextDocument doc) {
		if (cu == null) {
			return Collections.emptyList();
		}
		Collector<InlayHint> collector = new Collector<>();
		CompositeASTVisitor visitor = new CompositeASTVisitor();
		applicableInlayHintsProviders.forEach(p -> visitor.add(p.getInlayHintsComputer(jp, doc, cu, collector)));
		if (r != null) {
			if (r.getStart() != null) {
				visitor.setStartOffset(cu.getPosition(r.getStart().getLine(), r.getStart().getCharacter()));
			}
			if (r.getEnd() != null) {
				visitor.setEndOffset(cu.getPosition(r.getEnd().getLine(), r.getEnd().getCharacter()));
			}
		}
		cu.accept(visitor);
		return collector.get();
	}
}
