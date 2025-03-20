/*******************************************************************************
 * Copyright (c) 2022, 2025 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionResolveSupportCapabilities;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.java.handlers.JavaCodeActionHandler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtReconciler;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix.QuickfixData;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.BasicProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient;
import org.springframework.ide.vscode.commons.languageserver.util.LspClient.Client;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class RewriteCodeActionHandler implements JavaCodeActionHandler {
		
	private static final Logger log = LoggerFactory.getLogger(RewriteCodeActionHandler.class);
	
	final private CompilationUnitCache cuCache;
	final private JdtReconciler jdtReconciler;

	private BootJavaConfig config;

	private QuickfixRegistry quickfixRegistry;

	private DiagnosticSeverityProvider severityProvider;

	public RewriteCodeActionHandler(CompilationUnitCache cuCache, BootJavaConfig config, JdtReconciler jdtReconciler, QuickfixRegistry quickfixRegistry, DiagnosticSeverityProvider severityProvider) {
		this.cuCache = cuCache;
		this.config = config;
		this.jdtReconciler = jdtReconciler;
		this.quickfixRegistry = quickfixRegistry;
		this.severityProvider = severityProvider;
	}

	protected static boolean isResolve(CodeActionCapabilities capabilities, String property) {
		if (capabilities != null) {
			CodeActionResolveSupportCapabilities resolveSupport = capabilities.getResolveSupport();
			if (resolveSupport != null) {
				List<String> properties = resolveSupport.getProperties();
				return properties != null && properties.contains(property);
			}
		}
		return false;
	}
	
	private boolean isSupported(CodeActionCapabilities capabilities, CodeActionContext context) {
		// Default case is anything non-quick fix related.
		if (isResolve(capabilities, "edit")) {
			if (context.getOnly() != null) {
				return context.getOnly().contains(CodeActionKind.Refactor);
			} else {
				if (LspClient.currentClient() == Client.ECLIPSE) {
					// Eclipse would have no diagnostics in the context for QuickAssists refactoring. Diagnostics will be around for QuickFix only
					return context.getDiagnostics().isEmpty();
				} else {
					return true;
				}
				
			}
		}
		return false;
	}

	@Override
	public List<Either<Command, CodeAction>> handle(IJavaProject project, CancelChecker cancelToken,
			CodeActionCapabilities capabilities, CodeActionContext context, TextDocument doc, IRegion region) {
		// Short circuit here to avoid parsing the java source for nothing.
		if (!config.isJavaSourceReconcileEnabled()) {
			return Collections.emptyList();
		}
		
		try {

			URI uri = URI.create(doc.getUri());
			final QuickfixType rewriteFixType = quickfixRegistry.getQuickfixType(RewriteRefactorings.REWRITE_RECIPE_QUICKFIX);
			if (isSupported(capabilities, context) && rewriteFixType != null) {
				List<CodeAction> codeActions = cuCache.withCompilationUnit(project, uri, cu -> {
					if (cu != null) {
						try {
							List<CodeAction> cas = new ArrayList<>();
							List<ReconcileProblem> problems = new ArrayList<>();
							BasicProblemCollector problemsCollector = new BasicProblemCollector(problems);
							jdtReconciler.reconcile(project, uri, cu, problemsCollector, true, true);
							for (ReconcileProblem p : problems) {
								if (p.getOffset() <= region.getOffset() && p.getOffset() + p.getLength() >= region.getOffset() + region.getLength() && severityProvider.getDiagnosticSeverity(p) == null) {
									for (QuickfixData<?> qf :  p.getQuickfixes()) {
										if (qf.params instanceof FixDescriptor) {
											cas.add(createCodeActionFromScope((FixDescriptor) qf.params));
										}
									}
								}
							}
							return cas;
						} catch (Exception e) {
							log.error("", e);
						}
					}
					return Collections.emptyList();
				});

				return codeActions.stream().map(ca -> Either.<Command, CodeAction>forRight(ca)).collect(Collectors.toList());
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return Collections.emptyList();
	}

	private CodeAction createCodeActionFromScope(FixDescriptor d) {
		CodeAction ca = new CodeAction();
		ca.setKind(CodeActionKind.Refactor);
		ca.setTitle(d.getLabel());
		ca.setData(d);
		return ca;
	}
	
}
