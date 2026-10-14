/*******************************************************************************
 * Copyright (c) 2026 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.jdt.refactoring.JdtFixDescriptor;
import org.springframework.ide.vscode.boot.java.jdt.refactoring.JdtRefactorings;
import org.springframework.ide.vscode.boot.java.jdt.refactoring.RemoveModelAttributeNameRefactoring;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix.QuickfixData;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;

/**
 * Reconciler that detects {@code @ModelAttribute} method parameters whose explicit name is the
 * default name Spring derives from the parameter type anyway, e.g.
 * {@code @ModelAttribute("user") User account}, and offers a quickfix to remove the name.
 * <p>
 * Unlike for the web annotations checked by {@link ImplicitWebAnnotationNamesReconciler}, the
 * default name does not depend on the parameter name. Only parameter types for which that default
 * can be derived reliably are checked, everything else is skipped. The check is shared with the
 * quickfix, see {@link RemoveModelAttributeNameRefactoring#findRedundantName}.
 *
 * @author Nihal Kumar Sharma
 */
public class ImplicitModelAttributeNamesReconciler implements JdtAstReconciler {

	private static final String PROBLEM_LABEL = "Unnecessary `@ModelAttribute` name, it is the same as the default name derived from the parameter type";
	private static final String FIX_LABEL = "Remove implicit `@ModelAttribute` name";
	private static final String FIX_ALL_LABEL = "Remove implicit `@ModelAttribute` names in file";

	private final QuickfixRegistry registry;

	public ImplicitModelAttributeNamesReconciler(QuickfixRegistry registry) {
		this.registry = registry;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.MODEL_ATTRIBUTE_NAMES;
	}

	@Override
	public Optional<ASTVisitor> createVisitor(IJavaProject project, URI docUri, CompilationUnit cu, ReconcilingContext context) {
		List<ReconcileProblemImpl> problems = new ArrayList<>();
		List<RemoveModelAttributeNameRefactoring.Fix> fixes = new ArrayList<>();

		return Optional.of(new ASTVisitor() {

			@Override
			public boolean visit(SingleVariableDeclaration node) {
				for (Object modifier : node.modifiers()) {
					if (modifier instanceof Annotation annotation) {
						addProblemForRedundantName(annotation);
					}
				}
				return false;
			}

			private void addProblemForRedundantName(Annotation annotation) {
				StringLiteral name = RemoveModelAttributeNameRefactoring.findRedundantName(annotation);
				if (name != null) {
					ASTNode problemNode = name.getParent() instanceof MemberValuePair pair ? pair : name;
					ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(), PROBLEM_LABEL, problemNode.getStartPosition(), problemNode.getLength());

					RemoveModelAttributeNameRefactoring.Fix fix = new RemoveModelAttributeNameRefactoring.Fix(annotation.getStartPosition(), name.getLiteralValue());
					problem.addQuickfix(createQuickfix(docUri, new RemoveModelAttributeNameRefactoring(fix), FIX_LABEL, true));

					problems.add(problem);
					fixes.add(fix);
				}
			}

			@Override
			public void endVisit(CompilationUnit node) {
				// problems are only reported here, once they carry the quickfix for the whole file as well
				if (!problems.isEmpty()) {
					QuickfixData<JdtFixDescriptor> fixAll = createQuickfix(docUri, new RemoveModelAttributeNameRefactoring(fixes), FIX_ALL_LABEL, false);
					for (ReconcileProblemImpl problem : problems) {
						problem.addQuickfix(fixAll);
						context.getProblemCollector().accept(problem);
					}
				}
			}

		});
	}

	private QuickfixData<JdtFixDescriptor> createQuickfix(URI docUri, RemoveModelAttributeNameRefactoring refactoring, String label, boolean preferred) {
		JdtFixDescriptor descriptor = new JdtFixDescriptor(refactoring, List.of(docUri.toASCIIString()), label);
		return new QuickfixData<>(registry.getQuickfixType(JdtRefactorings.JDT_QUICKFIX), descriptor, label, preferred);
	}

}
